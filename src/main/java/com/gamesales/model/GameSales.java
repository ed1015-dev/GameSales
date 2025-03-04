package com.gamesales.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSales {
    @Id
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
