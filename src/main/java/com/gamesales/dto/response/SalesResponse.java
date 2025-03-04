package com.gamesales.dto.response;

import com.gamesales.dto.GameSalesDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesResponse {
    private List<GameSalesDTO> content;
    private int pageNumber;
    private int pageSize;
    private long executionTimeMs;
}