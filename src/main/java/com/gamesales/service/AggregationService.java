package com.gamesales.service;


import com.gamesales.dto.response.TotalSalesResponse;
import com.gamesales.model.DailySalesAggregate;
import com.gamesales.repository.DailySalesAggregateRepository;
import com.gamesales.repository.GameSalesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {

    private final DailySalesAggregateRepository dailySalesAggregateRepository;
    private final JdbcTemplate jdbcTemplate;
    private final GameSalesRepository gameSalesRepository;


    @Transactional
    public void aggregateAllData() {
        log.info("Starting data aggregation process");
        long startTime = System.currentTimeMillis();

        // Clear existing aggregates
        dailySalesAggregateRepository.deleteAll();

        // Aggregate by day for all games
        String aggregateByDaySql =
                "INSERT INTO daily_sales_aggregate (sale_date, game_no, total_count, total_amount) " +
                        "SELECT DATE(date_of_sale) as sale_date, NULL as game_no, COUNT(*) as total_count, SUM(sale_price) as total_amount " +
                        "FROM game_sales " +
                        "GROUP BY DATE(date_of_sale)";

        jdbcTemplate.execute(aggregateByDaySql);

        // Aggregate by day and game_no
        String aggregateByDayAndGameSql =
                "INSERT INTO daily_sales_aggregate (sale_date, game_no, total_count, total_amount) " +
                        "SELECT DATE(date_of_sale) as sale_date, game_no, COUNT(*) as total_count, SUM(sale_price) as total_amount " +
                        "FROM game_sales " +
                        "GROUP BY DATE(date_of_sale), game_no";

        jdbcTemplate.execute(aggregateByDayAndGameSql);

        gameSalesRepository.findAll();

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("Data aggregation completed in {} ms", executionTime);
    }

    public TotalSalesResponse getTotalSales(LocalDate fromDate, LocalDate toDate, Integer gameNo) {
        long startTime = System.currentTimeMillis();

        List<DailySalesAggregate> dailyAggregates;
        Map<LocalDate, Long> dailyCounts;
        Map<LocalDate, BigDecimal> dailySales;
        Map<LocalDate, BigDecimal> gameDailySales = null;

        dailyAggregates = dailySalesAggregateRepository.findBySaleDateBetweenAndGameNoIsNull(fromDate, toDate);

        // Map to response format
        dailyCounts = dailyAggregates.stream()
                .collect(Collectors.toMap(DailySalesAggregate::getSaleDate, DailySalesAggregate::getTotalCount));

        dailySales = dailyAggregates.stream()
                .collect(Collectors.toMap(DailySalesAggregate::getSaleDate, DailySalesAggregate::getTotalAmount));

        if (gameNo != null) {
            List<DailySalesAggregate> gameAggregates =
                    dailySalesAggregateRepository.findBySaleDateBetweenAndGameNo(fromDate, toDate, gameNo);

            gameDailySales = gameAggregates.stream()
                    .collect(Collectors.toMap(DailySalesAggregate::getSaleDate, DailySalesAggregate::getTotalAmount));
        }

        long executionTime = System.currentTimeMillis() - startTime;

        return TotalSalesResponse.builder()
                .dailyCounts(dailyCounts)
                .dailySales(dailySales)
                .gameDailySales(gameDailySales)
                .gameNo(gameNo)
                .fromDate(fromDate)
                .toDate(toDate)
                .executionTimeMs(executionTime)
                .build();
    }
}