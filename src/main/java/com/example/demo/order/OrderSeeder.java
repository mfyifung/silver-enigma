package com.example.demo.order;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Populates the {@code orders} table with realistic sample rows on first
 * boot. Idempotent: only inserts when the table is empty, so restarts never
 * duplicate rows.
 */
@Component
public class OrderSeeder implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public OrderSeeder(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return;
        }

        List<Order> seed = List.of(
                new Order("00001", "Amara Chen", "12 Riverside Ave, Austin, TX", "2026-06-30", "Accessories", "Completed"),
                new Order("00002", "Liam O'Brien", "88 Maple St, Portland, OR", "2026-07-01", "Mobile & Phone", "In Transit"),
                new Order("00003", "Priya Nair", "45 Elm Court, Seattle, WA", "2026-07-01", "Electronics", "Processing"),
                new Order("00004", "Diego Alvarez", "7 Birch Lane, Denver, CO", "2026-07-02", "Home & Living", "On Hold"),
                new Order("00005", "Sofia Rossi", "231 Pine Rd, Miami, FL", "2026-07-02", "Fashion & Beauty", "Completed"),
                new Order("00006", "Kenji Sato", "19 Cedar Blvd, San Jose, CA", "2026-07-03", "Services & Industry", "Rejected"),
                new Order("00007", "Grace Mensah", "5 Willow Way, Chicago, IL", "2026-07-03", "Book & Stationary", "In Transit"),
                new Order("00008", "Noah Kim", "310 Oak St, Boston, MA", "2026-07-04", "Health & Medicine", "Processing"),
                new Order("00009", "Ana Silva", "77 Spruce Ave, Phoenix, AZ", "2026-07-04", "Accessories", "Completed"),
                new Order("00010", "Ethan Brooks", "42 Aspen Dr, Raleigh, NC", "2026-07-05", "Mobile & Phone", "On Hold"),
                new Order("00011", "Mei Lin", "16 Poplar St, San Diego, CA", "2026-07-05", "Electronics", "Completed"),
                new Order("00012", "Omar Farouk", "58 Cherry Ln, Dallas, TX", "2026-07-06", "Home & Living", "Rejected"),
                new Order("00013", "Isabella Costa", "9 Magnolia Ct, Orlando, FL", "2026-07-06", "Fashion & Beauty", "In Transit"),
                new Order("00014", "Ravi Patel", "124 Sycamore Ave, Atlanta, GA", "2026-07-07", "Services & Industry", "Processing")
        );

        orderRepository.saveAll(seed);
    }
}
