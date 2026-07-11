package com.example.demo.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void roundTripsAnOrderThroughTheRepository() {
        Order saved = orderRepository.save(
                new Order("00099", "Test Person", "1 Test St", "2026-01-01", "Accessories", "Completed"));

        Order found = orderRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getOrderId()).isEqualTo("00099");
        assertThat(found.getName()).isEqualTo("Test Person");
        assertThat(found.getType()).isEqualTo("Accessories");
        assertThat(found.getStatus()).isEqualTo("Completed");
    }

    @Test
    void filterQueryReturnsOnlyTheExpectedSubset() {
        orderRepository.save(new Order("00100", "A", "addr", "2026-02-01", "Electronics", "Completed"));
        orderRepository.save(new Order("00101", "B", "addr", "2026-02-02", "Electronics", "On Hold"));
        orderRepository.save(new Order("00102", "C", "addr", "2026-02-03", "Accessories", "Completed"));

        List<Order> completedOnly = orderRepository.findAll(
                OrderSpecifications.combine(
                        OrderSpecifications.withStatuses(List.of("Completed")),
                        null,
                        null));

        assertThat(completedOnly)
                .extracting(Order::getOrderId)
                .contains("00100", "00102")
                .doesNotContain("00101");
        assertThat(completedOnly).allMatch(o -> o.getStatus().equals("Completed"));
    }
}
