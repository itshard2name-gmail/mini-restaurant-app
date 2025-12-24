package com.example.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private Long menuId;
    private String snapshotName;
    private BigDecimal snapshotPrice;
    private Integer quantity;
    private String notes;
}
