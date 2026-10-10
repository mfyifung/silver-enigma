package com.example.demo.order;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * {@code status} / {@code type} accept repeated params, CSV, or both mixed
     * (every value is split on ',' here); {@code date} is ISO {@code yyyy-MM-dd}.
     */
    @GetMapping
    public List<Order> list(@RequestParam(name = "status", required = false) List<String> status,
                            @RequestParam(name = "type", required = false) List<String> type,
                            @RequestParam(name = "date", required = false) String date) {
        return service.search(clean(status), clean(type), parseDate(date));
    }

    @GetMapping("/meta")
    public OrderService.OrderMeta meta() {
        return service.meta();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable("id") long id) {
        return service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }

    private static List<String> clean(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().filter(Objects::nonNull)
                .flatMap(v -> Arrays.stream(v.split(","))).map(String::trim).filter(v -> !v.isEmpty()).distinct().toList();
    }

    private static String parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date.trim()).toString();
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must be yyyy-MM-dd");
        }
    }
}
