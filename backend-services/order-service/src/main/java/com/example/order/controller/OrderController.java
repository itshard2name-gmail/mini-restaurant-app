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
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> createOrder(@RequestHeader("X-User-Id") String userId,
            @RequestBody CreateOrderRequest request) {
        if (userId == null || userId.isEmpty()) {
            // For now, if no header, maybe return 400 or allow anonymously?
            // User requirement says: "Through X-User-Id Header identify user"
            // Let's enforce it or check if Gateway passes it.
            // Since we implemented Auth-Service but didn't implement 'Gateway Token Relay'
            // fully (User didn't ask),
            // The frontend might need to pass X-User-Id or Gateway extracts it from JWT.
            // For simplicity (MVP), we assume the specific header is passed.
            // If Gateway doesn't strip prefix correctly, we might need adjustment.
            // Current Gateway config: /api/orders/** -> order-service.
            // If request is /api/orders, Gateway forwards /orders to order-service.
            // If request is /api/orders/menus, Gateway forwards /orders/menus.

            // Wait, if I map @RequestMapping("/orders"), then the forwarded path must be
            // /orders/...
            // The gateway predicate is Path=/api/orders/**. Filter StripPrefix=1.
            // So /api/orders/menus -> /orders/menus.
            // This matches @RequestMapping("/orders") + @GetMapping("/menus"). Correct.
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderService.createOrder(userId, request));
    }

    @GetMapping("/my")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader("X-User-Id") String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.payOrder(id));
    }
}
