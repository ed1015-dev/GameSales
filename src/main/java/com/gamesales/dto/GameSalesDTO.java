package com.gamesales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSalesDTO {
    private Long id;
    private Integer gameNo;
    private String gameName;
    private String gameCode;
    private Integer type;
    private BigDecimal costPrice;
    private BigDecimal tax;
    private BigDecimal salePrice;
    private LocalDateTime dateOfSale;
}