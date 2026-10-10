package com.example.demo.order;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query("select distinct o.type from Order o order by o.type")
    List<String> findDistinctTypes();

    @Query("select distinct o.status from Order o order by o.status")
    List<String> findDistinctStatuses();

    /** Specifications composed into a single SQL WHERE clause — filtering happens in the DB. */
    final class Filters {
        private Filters() {}

        static Specification<Order> statusIn(Collection<String> statuses) {
            return (root, q, cb) -> root.get("status").in(statuses);
        }

        static Specification<Order> typeIn(Collection<String> types) {
            return (root, q, cb) -> root.get("type").in(types);
        }

        static Specification<Order> dateEquals(String isoDate) {
            return (root, q, cb) -> cb.equal(root.get("date"), isoDate);
        }
    }
}
