package com.example.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
    private String notes;
    private String orderType; // DINE_IN, TAKEOUT
    private String tableNumber;
    private String guestToken; // Shadow Session ID for all users
    private String paymentMethod; // CASH, CREDIT_CARD, etc.
}
