package com.personal.finance.transaction;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public record TransactionQuery(
        TransactionType type,
        String category,
        String paymentMethod,
        String keyword,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        Integer page,
        Integer size
) {
}
