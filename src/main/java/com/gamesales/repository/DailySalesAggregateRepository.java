package com.gamesales.repository;

import com.gamesales.model.DailySalesAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySalesAggregateRepository extends JpaRepository<DailySalesAggregate, Long> {

    List<DailySalesAggregate> findBySaleDateBetweenAndGameNoIsNull(
            LocalDate fromDate, LocalDate toDate);


    List<DailySalesAggregate> findBySaleDateBetweenAndGameNo(
            LocalDate fromDate, LocalDate toDate, Integer gameNo);

}