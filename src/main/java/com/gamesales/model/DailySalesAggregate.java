package com.gamesales.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_sales_aggregate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate saleDate;

    private Integer gameNo; // Null for all games combined

    private Long totalCount;

    private BigDecimal totalAmount;
}