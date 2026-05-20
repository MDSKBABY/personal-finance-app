package com.personal.finance.statistics;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.personal.finance.transaction.TransactionRepository;
import com.personal.finance.transaction.TransactionResponse;
import com.personal.finance.transaction.TransactionType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsService {

    private final TransactionRepository transactionRepository;

    public StatisticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary(DateRangeQuery query) {
        DateRange range = resolveRange(query);
        BigDecimal income = transactionRepository.sumByTypeAndDateRange(
                TransactionType.income,
                range.startDate(),
                range.endDate()
        );
        BigDecimal expense = transactionRepository.sumByTypeAndDateRange(
                TransactionType.expense,
                range.startDate(),
                range.endDate()
        );
        List<TransactionResponse> recentTransactions = transactionRepository.findTop5ByOrderByTransactionDateDescIdDesc()
                .stream()
                .map(TransactionResponse::fromEntity)
                .toList();
        return new SummaryResponse(income, expense, income.subtract(expense), recentTransactions);
    }

    @Transactional(readOnly = true)
    public List<MonthlyStatisticsResponse> getMonthly(DateRangeQuery query) {
        DateRange range = resolveRange(query);
        return transactionRepository.sumMonthly(range.startDate(), range.endDate())
                .stream()
                .map(row -> {
                    BigDecimal income = toBigDecimal(row[1]);
                    BigDecimal expense = toBigDecimal(row[2]);
                    return new MonthlyStatisticsResponse(
                            row[0].toString(),
                            income,
                            expense,
                            income.subtract(expense)
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryStatisticsResponse> getCategory(DateRangeQuery query) {
        DateRange range = resolveRange(query);
        return transactionRepository.sumCategory(
                        TransactionType.expense.name(),
                        range.startDate(),
                        range.endDate()
                )
                .stream()
                .map(row -> new CategoryStatisticsResponse(row[0].toString(), toBigDecimal(row[1])))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DailyStatisticsResponse> getDaily(DateRangeQuery query) {
        DateRange range = resolveRange(query);
        return transactionRepository.sumDaily(range.startDate(), range.endDate())
                .stream()
                .map(row -> {
                    BigDecimal income = toBigDecimal(row[1]);
                    BigDecimal expense = toBigDecimal(row[2]);
                    return new DailyStatisticsResponse(toLocalDate(row[0]), income, expense, income.subtract(expense));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalaryStatisticsResponse> getSalary(DateRangeQuery query) {
        DateRange range = resolveRange(query);
        return transactionRepository.sumSalaryMonthly(range.startDate(), range.endDate())
                .stream()
                .map(row -> new SalaryStatisticsResponse(row[0].toString(), toBigDecimal(row[1])))
                .toList();
    }

    private DateRange resolveRange(DateRangeQuery query) {
        LocalDate startDate = query.startDate();
        LocalDate endDate = query.endDate();

        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = startDate == null ? currentMonth.atDay(1) : startDate;
            endDate = endDate == null ? currentMonth.atEndOfMonth() : endDate;
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        return new DateRange(startDate, endDate);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
