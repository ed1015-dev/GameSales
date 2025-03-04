package com.gamesales.repository;

import com.gamesales.model.GameSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameSalesRepository extends JpaRepository<GameSales, Long> {

    @Query(value = "SELECT * FROM game_sales WHERE  date_of_sale >= :fromDate AND date_of_sale <= :toDate ORDER BY date_of_sale ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<GameSales> findByDateOfSaleBetween(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = "SELECT * FROM game_sales  WHERE sale_price < :maxPrice ORDER BY date_of_sale ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<GameSales> findBySalePriceLessThan(
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = "SELECT * FROM game_sales WHERE sale_price > :minPrice ORDER BY date_of_sale ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<GameSales> findBySalePriceGreaterThan(
            @Param("minPrice") BigDecimal minPrice,
            @Param("limit") int limit,
            @Param("offset") int offset);

    // Get sales by date range and price less than given value
    @Query(value = "SELECT * FROM game_sales  WHERE (date_of_sale >= :fromDate AND date_of_sale <= :toDate) AND sale_price < :maxPrice ORDER BY date_of_sale ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<GameSales> findByDateOfSaleBetweenAndSalePriceLessThan(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("limit") int limit,
            @Param("offset") int offset);

    // Get sales by date range and price more than given value
    @Query(value = "SELECT * FROM game_sales  WHERE  (date_of_sale >= :fromDate AND date_of_sale <= :toDate) AND sale_price > :minPrice ORDER BY date_of_sale ASC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<GameSales> findByDateOfSaleBetweenAndSalePriceGreaterThan(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("minPrice") BigDecimal minPrice,
            @Param("limit") int limit,
            @Param("offset") int offset);

}