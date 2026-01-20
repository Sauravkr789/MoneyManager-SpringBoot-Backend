package com.example.expanse_tracker.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDTO {

    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; // e.g., "date", "amount", "name", etc.
    private String sortOrder; // "asc" or "desc"
}
