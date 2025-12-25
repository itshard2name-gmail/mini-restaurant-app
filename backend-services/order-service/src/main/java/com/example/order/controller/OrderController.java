package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.entity.Menu;
import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders") // Matches Gateway route /api/orders/** -> /orders/** implies we map /orders
                           // here
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/menus")
    public ResponseEntity<List<Menu>> getMenus() {
        return ResponseEntity.ok(orderService.getAllMenus());
    }

    @PostMapping("/menus")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(orderService.createMenu(menu));
    }

    @PutMapping("/menus/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        return ResponseEntity.ok(orderService.updateMenu(id, menu));
    }

    @DeleteMapping("/menus/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        orderService.deleteMenu(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody CreateOrderRequest request) {
        // userId is nullable for Guest Orders (DINE_IN only)
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String token) {
        return ResponseEntity.ok(orderService.getOrder(id, userId, token));
    }

    @GetMapping("/my")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    @GetMapping("/my/active")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getMyActiveOrders(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderService.getActiveOrders(userId));
    }

    @GetMapping("/my/history")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<org.springframework.data.domain.Page<Order>> getOrderHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderService.getOrderHistory(userId, page, size));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.payOrder(id));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Order> addItems(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String token,
            @RequestBody List<com.example.order.dto.OrderItemRequest> items) {
        return ResponseEntity.ok(orderService.addItems(id, userId, token, items));
    }

    @PatchMapping("/{id}/cancel")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId));
    }
}
