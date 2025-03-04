package com.gamesales.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalSalesResponse {
    private Map<LocalDate, Long> dailyCounts;
    private Map<LocalDate, BigDecimal> dailySales;
    private Map<LocalDate, BigDecimal> gameDailySales;
    private Integer gameNo;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long executionTimeMs;
}