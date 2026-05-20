package com.personal.finance.statistics;

import java.math.BigDecimal;

public record CategoryStatisticsResponse(
        String category,
        BigDecimal amount
) {
}
