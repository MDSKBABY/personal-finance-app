package com.personal.finance.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long>, JpaSpecificationExecutor<TransactionRecord> {

    @Query("""
            select coalesce(sum(t.amount), 0)
            from TransactionRecord t
            where t.type = :type
              and t.transactionDate between :startDate and :endDate
            """)
    BigDecimal sumByTypeAndDateRange(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<TransactionRecord> findTop5ByOrderByTransactionDateDescIdDesc();

    @Query(value = """
            select date_format(transaction_date, '%Y-%m') as month,
                   coalesce(sum(case when type = 'income' then amount else 0 end), 0) as income,
                   coalesce(sum(case when type = 'expense' then amount else 0 end), 0) as expense
            from transaction_records
            where transaction_date between :startDate and :endDate
            group by date_format(transaction_date, '%Y-%m')
            order by month
            """, nativeQuery = true)
    List<Object[]> sumMonthly(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = """
            select category, coalesce(sum(amount), 0) as amount
            from transaction_records
            where type = :type
              and transaction_date between :startDate and :endDate
            group by category
            order by amount desc
            """, nativeQuery = true)
    List<Object[]> sumCategory(
            @Param("type") String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
            select transaction_date,
                   coalesce(sum(case when type = 'income' then amount else 0 end), 0) as income,
                   coalesce(sum(case when type = 'expense' then amount else 0 end), 0) as expense
            from transaction_records
            where transaction_date between :startDate and :endDate
            group by transaction_date
            order by transaction_date
            """, nativeQuery = true)
    List<Object[]> sumDaily(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = """
            select date_format(transaction_date, '%Y-%m') as month,
                   coalesce(sum(amount), 0) as amount
            from transaction_records
            where type = 'income'
              and category = '工资'
              and transaction_date between :startDate and :endDate
            group by date_format(transaction_date, '%Y-%m')
            order by month
            """, nativeQuery = true)
    List<Object[]> sumSalaryMonthly(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
