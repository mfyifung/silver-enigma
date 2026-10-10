package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OrderSeeder implements CommandLineRunner {
    private final OrderRepository repo;

    public OrderSeeder(OrderRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        repo.save(new Order("00001", "Alice Johnson", "12 Oak St, Austin TX", "2026-09-01", "Accessories", "Completed"));
        repo.save(new Order("00002", "Bob Smith", "48 Pine Ave, Denver CO", "2026-09-02", "Mobile & Phone", "In Transit"));
        repo.save(new Order("00003", "Carla Gomez", "7 Elm Rd, Miami FL", "2026-09-02", "Electronics", "Processing"));
        repo.save(new Order("00004", "Dev Patel", "99 Lake Dr, Chicago IL", "2026-09-03", "Home & Living", "On Hold"));
        repo.save(new Order("00005", "Emma Wilson", "3 River Ln, Seattle WA", "2026-09-04", "Fashion & Beauty", "Completed"));
        repo.save(new Order("00006", "Frank Lee", "250 Main St, Boston MA", "2026-09-05", "Services & Industry", "Rejected"));
        repo.save(new Order("00007", "Grace Kim", "81 Hill Blvd, Portland OR", "2026-09-05", "Book & Stationary", "Completed"));
        repo.save(new Order("00008", "Henry Brown", "16 Cedar Ct, Phoenix AZ", "2026-09-06", "Health & Medicine", "In Transit"));
        repo.save(new Order("00009", "Isla Martin", "5 Maple Way, Atlanta GA", "2026-09-07", "Electronics", "Completed"));
        repo.save(new Order("00010", "Jack Davis", "62 Birch St, Dallas TX", "2026-09-08", "Accessories", "On Hold"));
        repo.save(new Order("00011", "Kara Nguyen", "27 Spruce Ave, San Jose CA", "2026-09-08", "Mobile & Phone", "Processing"));
        repo.save(new Order("00012", "Liam Scott", "140 Bay Rd, San Diego CA", "2026-09-09", "Fashion & Beauty", "Rejected"));
        repo.save(new Order("00013", "Mia Turner", "9 Park Pl, New York NY", "2026-09-10", "Home & Living", "In Transit"));
        repo.save(new Order("00014", "Noah Clark", "73 Sunset Blvd, Los Angeles CA", "2026-09-10", "Book & Stationary", "Processing"));
    }
}
