package com.personal.finance.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotNull(message = "请选择流水类型")
        TransactionType type,

        @NotBlank(message = "请选择分类")
        @Size(max = 50, message = "分类不能超过 50 个字符")
        String category,

        @NotNull(message = "请输入金额")
        @DecimalMin(value = "0.01", message = "金额必须大于 0")
        BigDecimal amount,

        @Size(max = 50, message = "支付方式不能超过 50 个字符")
        String paymentMethod,

        @NotNull(message = "请选择交易日期")
        LocalDate transactionDate,

        @Size(max = 255, message = "备注不能超过 255 个字符")
        String note
) {
}
