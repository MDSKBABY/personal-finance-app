package com.personal.finance.transaction;

import java.util.List;

import com.personal.finance.common.PageResult;
import com.personal.finance.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<TransactionResponse> findTransactions(TransactionQuery query) {
        validateDateRange(query);
        int page = query.page() == null || query.page() < 1 ? DEFAULT_PAGE : query.page();
        int size = query.size() == null || query.size() < 1 ? DEFAULT_SIZE : Math.min(query.size(), MAX_SIZE);
        PageRequest pageRequest = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "transactionDate", "id")
        );
        Page<TransactionRecord> result = transactionRepository.findAll(
                TransactionSpecifications.byQuery(query),
                pageRequest
        );
        List<TransactionResponse> records = result.getContent().stream()
                .map(TransactionResponse::fromEntity)
                .toList();
        return new PageResult<>(records, result.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        return TransactionResponse.fromEntity(findRecord(id));
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        TransactionRecord record = new TransactionRecord();
        fillRecord(record, request);
        return TransactionResponse.fromEntity(transactionRepository.save(record));
    }

    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        TransactionRecord record = findRecord(id);
        fillRecord(record, request);
        return TransactionResponse.fromEntity(transactionRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        TransactionRecord record = findRecord(id);
        transactionRepository.delete(record);
    }

    private TransactionRecord findRecord(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("流水记录不存在"));
    }

    private void fillRecord(TransactionRecord record, TransactionRequest request) {
        record.setType(request.type());
        record.setCategory(request.category().trim());
        record.setAmount(request.amount());
        record.setPaymentMethod(cleanText(request.paymentMethod()));
        record.setTransactionDate(request.transactionDate());
        record.setNote(cleanText(request.note()));
    }

    private void validateDateRange(TransactionQuery query) {
        if (query.startDate() != null && query.endDate() != null && query.startDate().isAfter(query.endDate())) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }

    private String cleanText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
