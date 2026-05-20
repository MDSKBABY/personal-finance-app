package com.personal.finance.statistics;

import java.math.BigDecimal;
import java.util.List;

import com.personal.finance.transaction.TransactionResponse;

public record SummaryResponse(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance,
        List<TransactionResponse> recentTransactions
) {
}
