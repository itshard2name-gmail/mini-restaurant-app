package com.example.order.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long menuId;
    private Integer quantity;
    private String notes;
}
