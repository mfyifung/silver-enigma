package com.example.demo.order;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Lists orders, optionally filtered by status/type/date. Filters are
     * combinable (ANDed across dimensions) and each of status/type accepts
     * multiple values, either as repeated query params
     * ({@code ?status=A&status=B}) or a single CSV value
     * ({@code ?status=A,B}) — both flatten to the same value list, which is
     * then OR-ed within its own dimension via a SQL {@code IN (...)}.
     * All filtering happens as a DB-level {@link Specification} — never
     * in-memory.
     */
    @GetMapping
    public List<Order> list(
            @RequestParam(name = "status", required = false) List<String> status,
            @RequestParam(name = "type", required = false) List<String> type,
            @RequestParam(name = "date", required = false) String date) {

        Specification<Order> spec = OrderSpecifications.combine(
                OrderSpecifications.withStatuses(splitCsv(status)),
                OrderSpecifications.withTypes(splitCsv(type)),
                OrderSpecifications.withDate(date));

        return orderRepository.findAll(spec);
    }

    /** Distinct filter-dropdown values actually present in the data — backend-driven, not hardcoded. */
    @GetMapping("/meta")
    public Map<String, List<String>> meta() {
        Map<String, List<String>> body = new LinkedHashMap<>();
        body.put("types", orderRepository.findDistinctTypes());
        body.put("statuses", orderRepository.findDistinctStatuses());
        return body;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Flattens repeated query params and/or CSV values into one flat list; null if none given. */
    private static List<String> splitCsv(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        List<String> out = new ArrayList<>();
        for (String value : raw) {
            for (String part : value.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    out.add(trimmed);
                }
            }
        }
        return out.isEmpty() ? null : out;
    }
}
