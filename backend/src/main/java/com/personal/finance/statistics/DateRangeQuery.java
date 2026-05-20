package com.personal.finance.statistics;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public record DateRangeQuery(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {
}
