package com.personal.finance.transaction;

import com.personal.finance.common.ApiResponse;
import com.personal.finance.common.PageResult;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ApiResponse<PageResult<TransactionResponse>> findTransactions(@ModelAttribute TransactionQuery query) {
        return ApiResponse.success(transactionService.findTransactions(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(transactionService.findById(id));
    }

    @PostMapping
    public ApiResponse<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success(transactionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return ApiResponse.success(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ApiResponse.success(null);
    }
}
