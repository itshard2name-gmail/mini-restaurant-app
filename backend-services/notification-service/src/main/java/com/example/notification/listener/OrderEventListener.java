package com.example.notification.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OrderEventListener {

    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public OrderEventListener(org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queuesToDeclare = @Queue("order.created.queue"))
    public void handleOrderEvent(Map<String, Object> orderEvent) {
        String status = (String) orderEvent.get("status");
        Long orderId = ((Number) orderEvent.get("orderId")).longValue();
        String userId = (String) orderEvent.get("userId");
        log.info("Received Order Event: ID={}, Status={}, UserId={}", orderId, status, userId);

        // Broadcast to user-specific topic
        if (userId != null) {
            String destination = "/topic/orders/user/" + userId;
            log.info("Broadcasting to destination: {}", destination);
            messagingTemplate.convertAndSend(destination, orderEvent);
        } else {
            log.warn("UserId is null, skipping broadcast for Order #{}", orderId);
        }

        // Broadcast to global topic for Admin Dashboard
        messagingTemplate.convertAndSend("/topic/orders", orderEvent);

        if ("PENDING".equals(status) || status == null) {
            log.info("[通知] 收到新訂單 #{}，已通知餐廳準備。", orderId);
        } else if ("PREPARING".equals(status)) {
            log.info("[通知] 訂單 #{} 製作中。", orderId);
        } else if ("READY".equals(status)) {
            log.info("[通知] 訂單 #{} 已製作完成，等待取餐。", orderId);
        } else if ("COMPLETED".equals(status)) {
            log.info("[通知] 訂單 #{} 已完成，請前往櫃檯取餐。", orderId);
        } else {
            log.info("[通知] 訂單 #{} 狀態更新: {}", orderId, status);
        }
    }
}
