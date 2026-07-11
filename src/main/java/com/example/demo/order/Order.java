package com.example.demo.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A single order row for the "Order Lists" admin feature.
 *
 * <p>{@code type} and {@code status} are plain strings constrained (at the
 * seeder / documentation level) to a fixed domain taxonomy rather than a Java
 * enum, so the JSON wire format matches the taxonomy values exactly (which
 * contain spaces and ampersands, e.g. "Mobile &amp; Phone").
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display id shown to end users, e.g. "00009". Distinct from the PK. */
    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    /** ISO {@code yyyy-MM-dd}. */
    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    protected Order() {
        // JPA
    }

    public Order(String orderId, String name, String address, String date, String type, String status) {
        this.orderId = orderId;
        this.name = name;
        this.address = address;
        this.date = date;
        this.type = type;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
