package com.example.demo.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderService.class)
class OrderRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired OrderRepository repository;
    @Autowired OrderService service;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.saveAll(List.of(
            new Order("10001", "A", "addr A", "2026-01-01", "Electronics", "Completed"),
            new Order("10002", "B", "addr B", "2026-01-02", "Electronics", "On Hold"),
            new Order("10003", "C", "addr C", "2026-01-01", "Home & Living", "Completed"),
            new Order("10004", "D", "addr D", "2026-01-03", "Accessories", "Rejected")));
        em.flush();
        em.clear();
    }

    @Test
    void roundTripsThroughTheRepository() {
        Order saved = repository.save(new Order("20001", "Round Trip", "1 Test Rd", "2026-02-14", "Mobile & Phone", "In Transit"));
        em.flush();
        em.clear();

        Order loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getOrderId()).isEqualTo("20001");
        assertThat(loaded.getName()).isEqualTo("Round Trip");
        assertThat(loaded.getAddress()).isEqualTo("1 Test Rd");
        assertThat(loaded.getDate()).isEqualTo("2026-02-14");
        assertThat(loaded.getType()).isEqualTo("Mobile & Phone");
        assertThat(loaded.getStatus()).isEqualTo("In Transit");
    }

    @Test
    void statusFilterReturnsOnlyMatchingRows() {
        assertThat(service.search(List.of("Completed"), List.of(), null))
            .extracting(Order::getOrderId).containsExactly("10001", "10003");
    }

    @Test
    void multiSelectStatusAndTypeFiltersCombine() {
        assertThat(service.search(List.of("Completed", "On Hold"), List.of("Electronics"), null))
            .extracting(Order::getOrderId).containsExactly("10001", "10002");
        assertThat(service.search(List.of(), List.of("Electronics", "Accessories"), null))
            .extracting(Order::getOrderId).containsExactly("10001", "10002", "10004");
    }

    @Test
    void dateFilterAndCombinationWithStatus() {
        assertThat(service.search(null, null, "2026-01-01"))
            .extracting(Order::getOrderId).containsExactly("10001", "10003");
        assertThat(service.search(List.of("Completed"), List.of("Home & Living"), "2026-01-01"))
            .extracting(Order::getOrderId).containsExactly("10003");
        assertThat(service.search(List.of("Rejected"), null, "2026-01-01")).isEmpty();
    }

    @Test
    void noFiltersReturnsAllRows() {
        assertThat(service.search(null, null, null)).hasSize(4);
    }

    @Test
    void distinctMetaValuesComeFromTheData() {
        assertThat(repository.findDistinctTypes()).containsExactly("Accessories", "Electronics", "Home & Living");
        assertThat(repository.findDistinctStatuses()).containsExactly("Completed", "On Hold", "Rejected");
    }
}
