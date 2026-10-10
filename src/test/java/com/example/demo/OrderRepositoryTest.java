package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:sqlite:target/test-orders.db",
    "spring.jpa.hibernate.ddl-auto=create-drop"})
class OrderRepositoryTest {
    @Autowired OrderRepository repo;

    private void seed() {
        repo.save(new Order("1", "A", "x", "2026-01-01", "Electronics", "Completed"));
        repo.save(new Order("2", "B", "x", "2026-01-02", "Accessories", "On Hold"));
        repo.save(new Order("3", "C", "x", "2026-01-01", "Electronics", "On Hold"));
    }

    @Test
    void roundTrip() {
        Order saved = repo.save(new Order("00009", "Zed", "addr", "2026-02-02", "Accessories", "Rejected"));
        Order found = repo.findById(saved.getId()).orElseThrow();
        assertThat(found.getOrderId()).isEqualTo("00009");
        assertThat(found.getStatus()).isEqualTo("Rejected");
    }

    @Test
    void filters() {
        seed();
        assertThat(repo.search(true, List.of("On Hold"), false, List.of(""), null)).hasSize(2);
        assertThat(repo.search(false, List.of(""), true, List.of("Electronics"), null)).hasSize(2);
        assertThat(repo.search(true, List.of("On Hold"), true, List.of("Electronics"), null))
            .extracting(Order::getOrderId).containsExactly("3");
        assertThat(repo.search(true, List.of("Completed", "On Hold"), false, List.of(""), "2026-01-01")).hasSize(2);
        assertThat(repo.search(false, List.of(""), false, List.of(""), null)).hasSize(3);
        assertThat(repo.distinctTypes()).containsExactly("Accessories", "Electronics");
    }
}
