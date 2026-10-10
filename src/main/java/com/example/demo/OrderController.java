package com.example.demo;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository repo;

    public OrderController(OrderRepository repo) { this.repo = repo; }

    /** Accepts repeated params and/or CSV values; returns cleaned list. */
    private static List<String> flatten(List<String> raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) return out;
        for (String r : raw)
            for (String p : r.split(",")) if (!p.isBlank()) out.add(p.trim());
        return out;
    }

    @GetMapping
    public List<Order> list(@RequestParam(required = false) List<String> status,
                            @RequestParam(required = false) List<String> type,
                            @RequestParam(required = false) String date) {
        List<String> s = flatten(status), t = flatten(type);
        String d = (date == null || date.isBlank()) ? null : date.trim();
        return repo.search(!s.isEmpty(), s.isEmpty() ? List.of("") : s,
                           !t.isEmpty(), t.isEmpty() ? List.of("") : t, d);
    }

    @GetMapping("/meta")
    public Map<String, List<String>> meta() {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("types", repo.distinctTypes());
        m.put("statuses", repo.distinctStatuses());
        return m;
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
