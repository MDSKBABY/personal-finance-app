package com.personal.finance.statistics;

import java.math.BigDecimal;

public record MonthlyStatisticsResponse(
        String month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}
