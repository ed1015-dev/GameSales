package com.gamesales.controller;

import com.gamesales.model.ImportJob;
import com.gamesales.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GameImportController {

    private final ImportService importService;

    @PostMapping("/import")
    public ResponseEntity<ImportJob> importCsvFile(@RequestParam("file") MultipartFile file) {
        ImportJob newImportJob = new ImportJob();
        if (file.isEmpty()) {
            newImportJob.setStatus("FAILED");
            newImportJob.setErrorMessage("No file input found");
            return ResponseEntity.badRequest().body(newImportJob);
        }

        log.info("Receive file import request");

        try {
            long startTime = System.currentTimeMillis();
            ImportJob importJob = importService.importCsvFile(file);
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("CSV import completed in {} ms", executionTime);
            return ResponseEntity.ok(importJob);
        } catch (Exception e) {
            log.error("Failed to import CSV", e);
            newImportJob.setErrorMessage(e.toString());
            newImportJob.setStatus("FAILED");
            return ResponseEntity.internalServerError().body(newImportJob);
        }
    }
}