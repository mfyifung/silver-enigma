package com.example.demo.order;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic, DB-level filter predicates for {@link Order}. Each factory returns
 * a {@link Specification} that Spring Data JPA translates into a SQL
 * {@code WHERE} clause executed by the database — filtering never happens
 * in-memory in the controller.
 */
public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    /** {@code status IN (...)}, or {@code null} (no-op predicate) when the list is empty. */
    public static Specification<Order> withStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    /** {@code type IN (...)}, or {@code null} (no-op predicate) when the list is empty. */
    public static Specification<Order> withTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> root.get("type").in(types);
    }

    /** {@code date = ?}, or {@code null} (no-op predicate) when absent/blank. */
    public static Specification<Order> withDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("date"), date);
    }

    /** ANDs every non-null spec together; returns "match everything" if all are absent. */
    public static Specification<Order> combine(Specification<Order> statusSpec,
                                                Specification<Order> typeSpec,
                                                Specification<Order> dateSpec) {
        Specification<Order> combined = Specification.where(null);
        if (statusSpec != null) {
            combined = combined.and(statusSpec);
        }
        if (typeSpec != null) {
            combined = combined.and(typeSpec);
        }
        if (dateSpec != null) {
            combined = combined.and(dateSpec);
        }
        return combined;
    }
}
