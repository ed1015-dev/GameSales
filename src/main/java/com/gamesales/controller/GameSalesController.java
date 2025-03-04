package com.gamesales.controller;

import com.gamesales.dto.request.GameSalesRequest;
import com.gamesales.dto.request.TotalSalesRequest;
import com.gamesales.dto.response.SalesResponse;
import com.gamesales.dto.response.TotalSalesResponse;
import com.gamesales.service.AggregationService;
import com.gamesales.service.GameSalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GameSalesController {

    private final GameSalesService gameSalesService;
    private final AggregationService aggregationService;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @PostMapping("/getGameSales")
    public ResponseEntity<SalesResponse> getGameSales(@RequestBody GameSalesRequest request) {

        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        BigDecimal price = request.getPrice();
        boolean lessThan = request.isLessThan();
        int page = request.getPage();
        int size = DEFAULT_PAGE_SIZE;

        SalesResponse response;

        if (fromDate != null && toDate != null && price != null) {
            LocalDateTime fromDateTime = fromDate.atStartOfDay();
            LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);

            response = gameSalesService.getGameSalesByDateRangeAndPrice(
                    fromDateTime, toDateTime, price, lessThan, page, size);
        } else if (fromDate != null && toDate != null) {
            LocalDateTime fromDateTime = fromDate.atStartOfDay();
            LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);

            response = gameSalesService.getGameSalesByDateRange(
                    fromDateTime, toDateTime, page, size);
        } else if (price != null) {
            response = gameSalesService.getGameSalesByPrice(
                    price, lessThan, page, size);
        } else {
            response = gameSalesService.getAllGameSales(page, size);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getTotalSales")
    public ResponseEntity<TotalSalesResponse> getTotalSales(@RequestBody TotalSalesRequest request) {

        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();
        Integer gameNo = request.getGameNo();

        TotalSalesResponse response = aggregationService.getTotalSales(fromDate, toDate, gameNo);
        return ResponseEntity.ok(response);
    }
}