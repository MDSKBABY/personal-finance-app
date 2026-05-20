package com.personal.finance.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStatisticsResponse(
        LocalDate date,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal balance
) {
}
