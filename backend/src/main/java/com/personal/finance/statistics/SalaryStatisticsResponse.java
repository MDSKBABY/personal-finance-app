package com.personal.finance.statistics;

import java.math.BigDecimal;

public record SalaryStatisticsResponse(
        String month,
        BigDecimal amount
) {
}
