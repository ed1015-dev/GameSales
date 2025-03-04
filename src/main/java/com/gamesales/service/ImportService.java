package com.gamesales.service;

import com.gamesales.model.ImportJob;
import com.gamesales.repository.ImportJobRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ImportJobRepository importJobRepository;
    private final AggregationService aggregationService;
    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 10000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ImportJob importCsvFile(MultipartFile file) throws IOException {
        long startTime = System.currentTimeMillis();

        ImportJob importJob = ImportJob.builder()
                .filename(file.getOriginalFilename())
                .startTime(LocalDateTime.now())
                .status("PROCESSING")
                .processedRecords(0L)
                .failedRecords(0L)
                .build();

        importJob = importJobRepository.save(importJob);

        final Long importJobId = importJob.getId(); // Capture ID for async operations

        log.info("Starting importing CSV jobID : {}", importJobId);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            List<CSVRecord> allRecords = csvParser.getRecords();
            long totalRecords = allRecords.size();

            long csvParserExecutionTime = System.currentTimeMillis() - startTime;
            log.info("parse all CSV completed in {} ms", csvParserExecutionTime);

            log.info("Total records for CSV: {}", totalRecords);
            importJob.setTotalRecords(totalRecords);
            importJobRepository.save(importJob);

            int availableProcessors = Runtime.getRuntime().availableProcessors();
            int threadPoolSize = Math.min(availableProcessors, 8); // Cap at 8 threads
            ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

            // Shared counters for tracking progress
            AtomicLong processedCount = new AtomicLong(0);
            AtomicLong failedCount = new AtomicLong(0);

            // Split records into chunks for parallel processing
            int chunkSize = Math.min(BATCH_SIZE, (int) Math.ceil((double) totalRecords / threadPoolSize));
            List<List<CSVRecord>> recordBatches = new ArrayList<>();

            List<CSVRecord> currentBatch = new ArrayList<>();
            for (CSVRecord record : allRecords) {
                currentBatch.add(record);
                if (currentBatch.size() >= chunkSize) {
                    recordBatches.add(new ArrayList<>(currentBatch));
                    currentBatch.clear();
                }
            }

            long addBatchExecutionTime = System.currentTimeMillis() - startTime;
            log.info("add all records to batch completed in {} ms", addBatchExecutionTime);

            // Add remaining records
            if (!currentBatch.isEmpty()) {
                recordBatches.add(currentBatch);
            }



            CompletionService<BatchResult> completionService = getBatchResultCompletionService(executorService, recordBatches, importJobId, chunkSize);

            // Schedule a task to update progress
            ScheduledExecutorService progressUpdater = Executors.newSingleThreadScheduledExecutor();
            progressUpdater.scheduleAtFixedRate(() -> {
                ImportJob job = importJobRepository.findById(importJobId).orElse(null);
                if (job != null && "PROCESSING".equals(job.getStatus())) {
                    job.setProcessedRecords(processedCount.get());
                    job.setFailedRecords(failedCount.get());
                    importJobRepository.save(job);
//                    importJobRepository.flush();
                    log.info("current job processing : {}", job);
                }
            }, 2, 2, TimeUnit.SECONDS);

            // Collect results as tasks complete
            try {
                for (int i = 0; i < recordBatches.size(); i++) {
                    Future<BatchResult> future = completionService.take();
                    BatchResult result = future.get();
                    processedCount.addAndGet(result.getProcessedCount());
                    failedCount.addAndGet(result.getFailedCount());
                    log.info("processed count : {} , failed count: {}", processedCount, failedCount);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while collecting thread results", e);
                importJob.setErrorMessage("Threading error: " + e.getMessage());
            } finally {
                // Shutdown executors
                progressUpdater.shutdown();
                executorService.shutdown();

                try {
                    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                    if (!progressUpdater.awaitTermination(5, TimeUnit.SECONDS)) {
                        progressUpdater.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executorService.shutdownNow();
                    progressUpdater.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            // Update import job with final results
            importJob = importJobRepository.findById(importJobId).orElse(importJob);
            importJob.setProcessedRecords(processedCount.get());
            importJob.setFailedRecords(failedCount.get());
            importJob.setEndTime(LocalDateTime.now());
            importJob.setStatus("COMPLETED");


        } catch (Exception e) {
            importJob.setStatus("FAILED");
            importJob.setEndTime(LocalDateTime.now());
            importJob.setErrorMessage(e.getMessage());
            log.error("Import failed", e);
        }

        // Calculate execution time and update the job
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("Import completed in {} ms", executionTime);


        ImportJob result = importJobRepository.save(importJob);
//        importJobRepository.flush();
        CompletableFuture.runAsync(aggregationService::aggregateAllData);
        return result;
    }

    private CompletionService<BatchResult> getBatchResultCompletionService(ExecutorService executorService, List<List<CSVRecord>> recordBatches, Long importJobId, int chunkSize) {
        final String sql = "INSERT INTO game_sales (id, game_no, game_name, game_code, type, cost_price, tax, sale_price, date_of_sale) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Create a completion service to track task completion
        CompletionService<BatchResult> completionService =
                new ExecutorCompletionService<>(executorService);

        // Submit each batch for processing
        int counter = 0;
        for (List<CSVRecord> batch : recordBatches) {
            counter++;
            int finalCounter = counter;
            completionService.submit(() -> processBatch(batch, sql, importJobId, finalCounter, chunkSize));

        }
        return completionService;
    }

    /**
     * Process a batch of CSV records in a separate thread
     */
    private BatchResult processBatch(List<CSVRecord> records, String sql, Long importJobId, int counter, int chunkSize) {
        long startTime = System.currentTimeMillis();
        long processedCount = 0;
        long failedCount = 0;
        List<Object[]> batch = new ArrayList<>(chunkSize);

        for (CSVRecord record : records) {
            try {
                Object[] values = new Object[9];

                values[0] = Long.parseLong(record.get("id")); // id
                values[1] = Integer.parseInt(record.get("game_no")); // game_no
                values[2] = record.get("game_name"); // game_name
                values[3] = record.get("game_code"); // game_code
                values[4] = Integer.parseInt(record.get("type")); // type
                values[5] = new BigDecimal(record.get("cost_price")); // cost_price
                values[6] = new BigDecimal(record.get("tax")); // tax
                values[7] = new BigDecimal(record.get("sale_price")); // sale_price
                values[8] = LocalDateTime.parse(record.get("date_of_sale"), DATE_FORMATTER); // date_of_sale

                // Add to batch
                batch.add(values);

                // If batch size reached, execute batch
                if (batch.size() >= BATCH_SIZE) {
                    jdbcTemplate.batchUpdate(sql, batch);
                    processedCount += batch.size();
                    batch.clear();
                }
            } catch (Exception e) {
                failedCount++;
                log.error("Error processing record: {}", record.toString(), e);
            }
        }

        // Process remaining records
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            processedCount += batch.size();
        }

        long batchUpdateExecutionTime = System.currentTimeMillis() - startTime;
        log.info("importId #{} batch #{} completed in {} ms", importJobId, counter, batchUpdateExecutionTime);

        return new BatchResult(processedCount, failedCount);
    }

    @Data
    @AllArgsConstructor
    private static class BatchResult {
        private long processedCount;
        private long failedCount;
    }
}