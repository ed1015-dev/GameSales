package com.gamesales.service;


import com.gamesales.dto.GameSalesDTO;
import com.gamesales.dto.response.SalesResponse;
import com.gamesales.model.GameSales;
import com.gamesales.repository.GameSalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSalesService {

    private final GameSalesRepository gameSalesRepository;

    public SalesResponse getAllGameSales(int page, int size) {
        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(page, size);
        Page<GameSales> salesPage = gameSalesRepository.findAll(pageable);

        List<GameSalesDTO> salesDTOs = salesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        long executionTime = System.currentTimeMillis() - startTime;

        return SalesResponse.builder()
                .content(salesDTOs)
                .pageNumber(salesPage.getNumber())
                .pageSize(salesPage.getSize())
                .executionTimeMs(executionTime)
                .build();
    }

    public SalesResponse getGameSalesByDateRange(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size) {

        long startTime = System.currentTimeMillis();

        List<GameSales> salesPage = gameSalesRepository.findByDateOfSaleBetween(
                fromDate, toDate, size, page);

        List<GameSalesDTO> salesDTOs = salesPage.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        long executionTime = System.currentTimeMillis() - startTime;

        return SalesResponse.builder()
                .content(salesDTOs)
                .pageNumber(page)
                .pageSize(size)  // No count query executed
                .executionTimeMs(executionTime)
                .build();
    }

    public SalesResponse getGameSalesByPrice(
            BigDecimal price,
            boolean lessThan,
            int page,
            int size) {

        long startTime = System.currentTimeMillis();

        List<GameSales> salesList;

        if (lessThan) {
            salesList = gameSalesRepository.findBySalePriceLessThan(price, size, page);
        } else {
            salesList = gameSalesRepository.findBySalePriceGreaterThan(price, size, page);
        }

        List<GameSalesDTO> salesDTOs = salesList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        long executionTime = System.currentTimeMillis() - startTime;

        return SalesResponse.builder()
                .content(salesDTOs)
                .pageNumber(page)
                .pageSize(size) // No count query executed
                .executionTimeMs(executionTime)
                .build();
    }

    public SalesResponse getGameSalesByDateRangeAndPrice(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            BigDecimal price,
            boolean lessThan,
            int page,
            int size) {

        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(page, size);
        List<GameSales> salesPage;

        if (lessThan) {
            salesPage = gameSalesRepository.findByDateOfSaleBetweenAndSalePriceLessThan(
                    fromDate, toDate, price, size, page);
        } else {
            salesPage = gameSalesRepository.findByDateOfSaleBetweenAndSalePriceGreaterThan(
                    fromDate, toDate, price, size, page);
        }

        long sqlExecutionTime = System.currentTimeMillis() - startTime;

        log.info("game sales query took : {} ms", sqlExecutionTime);
        List<GameSalesDTO> salesDTOs = salesPage.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        long executionTime = System.currentTimeMillis() - startTime;

        return SalesResponse.builder()
                .content(salesDTOs)
                .pageNumber(page)
                .pageSize(size)
                .executionTimeMs(executionTime)
                .build();
    }

    private GameSalesDTO convertToDTO(GameSales entity) {
        return GameSalesDTO.builder()
                .id(entity.getId())
                .gameNo(entity.getGameNo())
                .gameName(entity.getGameName())
                .gameCode(entity.getGameCode())
                .type(entity.getType())
                .costPrice(entity.getCostPrice())
                .tax(entity.getTax())
                .salePrice(entity.getSalePrice())
                .dateOfSale(entity.getDateOfSale())
                .build();
    }
}