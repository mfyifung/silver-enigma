package com.example.demo.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns orders matching every supplied filter (AND across filters, OR within a
     * multi-valued filter). Null/empty filters are ignored. {@code isoDate} must already
     * be validated as {@code yyyy-MM-dd}.
     */
    public List<Order> search(List<String> statuses, List<String> types, String isoDate) {
        Specification<Order> spec = Specification.where(null);
        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and(OrderRepository.Filters.statusIn(statuses));
        }
        if (types != null && !types.isEmpty()) {
            spec = spec.and(OrderRepository.Filters.typeIn(types));
        }
        if (isoDate != null) {
            spec = spec.and(OrderRepository.Filters.dateEquals(isoDate));
        }
        return repository.findAll(spec, Sort.by("orderId"));
    }

    public Optional<Order> findById(long id) {
        return repository.findById(id);
    }

    public OrderMeta meta() {
        return new OrderMeta(repository.findDistinctTypes(), repository.findDistinctStatuses());
    }

    public record OrderMeta(List<String> types, List<String> statuses) {}
}
