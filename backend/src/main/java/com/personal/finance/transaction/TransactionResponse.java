package com.personal.finance.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        String category,
        BigDecimal amount,
        String paymentMethod,
        LocalDate transactionDate,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TransactionResponse fromEntity(TransactionRecord record) {
        return new TransactionResponse(
                record.getId(),
                record.getType(),
                record.getCategory(),
                record.getAmount(),
                record.getPaymentMethod(),
                record.getTransactionDate(),
                record.getNote(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
