package com.example.demo.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A persisted order row. {@code date} is stored as an ISO-8601 {@code yyyy-MM-dd}
 * string: ISO dates sort and compare correctly as text, and it avoids the SQLite
 * driver's epoch-millis date encoding (time-zone sensitive).
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 16)
    private String orderId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "order_date", nullable = false, length = 10)
    private String date;

    @Column(nullable = false, length = 64)
    private String type;

    @Column(nullable = false, length = 32)
    private String status;

    protected Order() {
        // for JPA
    }

    public Order(String orderId, String name, String address, String date, String type, String status) {
        this.orderId = orderId;
        this.name = name;
        this.address = address;
        this.date = date;
        this.type = type;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public String getStatus() { return status; }
}
