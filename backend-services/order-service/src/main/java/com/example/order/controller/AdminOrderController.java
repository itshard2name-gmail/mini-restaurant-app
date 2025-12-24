package com.example.order.controller;

import com.example.order.entity.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders/admin")
public class AdminOrderController {

    private final com.example.order.service.OrderService orderService;

    public AdminOrderController(com.example.order.service.OrderService orderService) {
        this.orderService = orderService;
    }

    // Constructor with repository is deprecated/removed in favor of Service
    // adapting to keep compiling if Spring expects strict injection,
    // better to just replace the field and constructor entirely.

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        // ideally service should provide this too, but for reading all, repo is fine if
        // we had it.
        // Let's rely on repo for read if we must, or adding getAllOrders to service.
        // I'll add getAll to Service to be clean.
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
            @RequestBody java.util.Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        if (newStatus == null
                || (!newStatus.equals("PREPARING") && !newStatus.equals("READY") && !newStatus.equals("COMPLETED"))) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(orderService.updateStatus(id, newStatus));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<Order>> searchOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(orderService.searchOrders(page, size, status, date, query));
    }
}
