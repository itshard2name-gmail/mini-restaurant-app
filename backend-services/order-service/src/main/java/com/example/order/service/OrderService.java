package com.example.order.service;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.entity.Menu;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.repository.MenuRepository;
import com.example.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public OrderService(MenuRepository menuRepository, OrderRepository orderRepository,
            org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public List<Order> getMyOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getActiveOrders(String userId) {
        List<String> statuses = List.of("PENDING", "PAID", "PREPARING", "READY");
        return orderRepository.findByUserIdAndStatusIn(userId, statuses, org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public org.springframework.data.domain.Page<Order> getOrderHistory(String userId, int page, int size) {
        List<String> statuses = List.of("COMPLETED", "CANCELLED");
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));
        return orderRepository.findByUserIdAndStatusIn(userId, statuses, pageable);
    }

    @Transactional
    public Order createOrder(String userId, CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PENDING"); // Default to PENDING

        // Map OrderType
        try {
            if (request.getOrderType() != null) {
                com.example.order.entity.OrderType type = com.example.order.entity.OrderType
                        .valueOf(request.getOrderType());
                order.setOrderType(type);

                // Validate Table Number for Dine-In
                if (type == com.example.order.entity.OrderType.DINE_IN) {
                    if (request.getTableNumber() == null || request.getTableNumber().trim().isEmpty()) {
                        throw new IllegalArgumentException("Table number is required for Dine-In orders");
                    }
                    order.setTableNumber(request.getTableNumber());
                } else {
                    order.setTableNumber(null); // Clear table number for Takeout
                }
            } else {
                // Default to TAKEOUT if not specified for backward compatibility
                order.setOrderType(com.example.order.entity.OrderType.TAKEOUT);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Order Type: " + request.getOrderType());
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty");
        }

        for (OrderItemRequest itemRequest : request.getItems()) {
            Menu menu = menuRepository.findById(java.util.Objects.requireNonNull(itemRequest.getMenuId()))
                    .orElseThrow(() -> new RuntimeException("Menu not found: " + itemRequest.getMenuId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuId(menu.getId());
            orderItem.setSnapshotName(menu.getName());
            orderItem.setSnapshotPrice(menu.getPrice());
            orderItem.setQuantity(
                    java.util.Objects.requireNonNull(itemRequest.getQuantity(), "Quantity cannot be null"));
            orderItem.setNotes(itemRequest.getNotes());

            BigDecimal itemTotal = menu.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);

        // Send RabbitMQ message
        sendOrderEvent(savedOrder);

        return savedOrder;
    }

    @Transactional
    public Order payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus("PAID");
        Order savedOrder = orderRepository.save(order);
        sendOrderEvent(savedOrder);
        return savedOrder;
    }

    @Transactional
    public Order updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        // Send RabbitMQ message for notification
        sendOrderEvent(savedOrder);

        return savedOrder;
    }

    private void sendOrderEvent(Order order) {
        try {
            java.util.Map<String, Object> event = new java.util.HashMap<>();
            event.put("orderId", order.getId());
            event.put("userId", order.getUserId());
            event.put("status", order.getStatus());
            event.put("totalPrice", order.getTotalPrice());
            rabbitTemplate.convertAndSend(com.example.order.config.RabbitConfig.EXCHANGE_NAME,
                    com.example.order.config.RabbitConfig.ROUTING_KEY, event);
        } catch (Exception e) {
            System.err.println("Failed to send RabbitMQ message: " + e.getMessage());
        }
    }

    @Transactional
    public Menu createMenu(Menu menu) {
        return menuRepository.save(menu);
    }

    @Transactional
    public Menu updateMenu(Long id, Menu menuDetails) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found: " + id));

        menu.setName(menuDetails.getName());
        menu.setPrice(menuDetails.getPrice());
        menu.setDescription(menuDetails.getDescription());
        menu.setImageUrl(menuDetails.getImageUrl());

        return menuRepository.save(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found: " + id));
        menuRepository.delete(menu);
    }

    @Transactional
    public Order cancelOrder(Long orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You do not own this order");
        }

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus("CANCELLED");
        Order savedOrder = orderRepository.save(order);

        // Send RabbitMQ message
        sendOrderEvent(savedOrder);

        return savedOrder;
    }

    public org.springframework.data.domain.Page<Order> searchOrders(int page, int size, String status, String date,
            String query) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt"));

        // Normalize query to lowercase for case-insensitive search if present
        String finalQuery = (query != null && !query.trim().isEmpty()) ? query.toLowerCase().trim() : null;
        String finalDate = (date != null && !date.trim().isEmpty()) ? date.trim() : null;
        String finalStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        return orderRepository.findOrders(finalStatus, finalDate, finalQuery, pageable);
    }
}
