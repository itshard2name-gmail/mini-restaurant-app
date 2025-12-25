package com.example.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@Table(name = "orders") // 'order' is a reserved keyword in SQL
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String userId; // From X-User-Id header (Nullable for Guest)

    private String guestToken; // Random UUID for Guest Access

    private BigDecimal totalPrice;
    private String status; // PENDING, COMPLETED, etc.

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private String tableNumber;

    private java.time.Instant createdAt = java.time.Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}
