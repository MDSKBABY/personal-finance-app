package com.personal.finance.transaction;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<TransactionRecord> byQuery(TransactionQuery query) {
        return (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.type() != null) {
                predicates.add(builder.equal(root.get("type"), query.type()));
            }
            if (hasText(query.category())) {
                predicates.add(builder.equal(root.get("category"), query.category().trim()));
            }
            if (hasText(query.paymentMethod())) {
                predicates.add(builder.equal(root.get("paymentMethod"), query.paymentMethod().trim()));
            }
            if (hasText(query.keyword())) {
                predicates.add(builder.like(root.get("note"), "%" + query.keyword().trim() + "%"));
            }
            if (query.startDate() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("transactionDate"), query.startDate()));
            }
            if (query.endDate() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("transactionDate"), query.endDate()));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
