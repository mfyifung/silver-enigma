package com.example.demo.order;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds demo orders on startup when the table is empty (idempotent across restarts). */
@Component
public class OrderSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OrderSeeder.class);

    private final OrderRepository repository;

    public OrderSeeder(OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        long existing = repository.count();
        if (existing > 0) {
            log.info("orders table already has {} rows; skipping seed", existing);
            return;
        }
        List<Order> seed = List.of(
            new Order("00001", "Christine Brooks", "089 Kutch Green Apt. 448", "2026-09-04", "Electronics", "Completed"),
            new Order("00002", "Rosie Pearson", "979 Immanuel Ferry Suite 526", "2026-05-28", "Book & Stationary", "Processing"),
            new Order("00003", "Darrell Caldwell", "8587 Frida Ports", "2026-11-23", "Mobile & Phone", "Rejected"),
            new Order("00004", "Gilbert Johnston", "768 Destiny Lake Suite 600", "2026-02-05", "Accessories", "Completed"),
            new Order("00005", "Alan Cain", "042 Mylene Throughway", "2026-07-29", "Home & Living", "Processing"),
            new Order("00006", "Alfred Murray", "543 Weimann Mountain", "2026-08-15", "Fashion & Beauty", "Completed"),
            new Order("00007", "Maggie Sullivan", "New Scottieberg", "2026-12-21", "Services & Industry", "Processing"),
            new Order("00008", "Rosie Todd", "New Jon", "2026-04-30", "Health & Medicine", "On Hold"),
            new Order("00009", "Dollie Hines", "124 Lyla Forge Suite 975", "2026-01-09", "Electronics", "In Transit"),
            new Order("00010", "Ethel Morgan", "77 Harvest Lane", "2026-09-04", "Mobile & Phone", "In Transit"),
            new Order("00011", "Henry Wallace", "310 Oak Ridge Blvd", "2026-03-17", "Book & Stationary", "On Hold"),
            new Order("00012", "Nina Patel", "5 Riverside Close", "2026-06-12", "Accessories", "Rejected"),
            new Order("00013", "Omar Haddad", "221 Elm Street", "2026-09-04", "Health & Medicine", "Completed"),
            new Order("00014", "Lucy Chen", "48 Willow Court", "2026-10-01", "Home & Living", "In Transit")
        );
        repository.saveAll(seed);
        log.info("seeded {} orders", seed.size());
    }
}
