package com.example.notification.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OrderEventListener {

    @RabbitListener(queuesToDeclare = @Queue("order.created.queue"))
    public void handleOrderEvent(Map<String, Object> orderEvent) {
        String status = (String) orderEvent.get("status");
        Long orderId = ((Number) orderEvent.get("orderId")).longValue();

        if ("PENDING".equals(status) || status == null) {
            log.info("[通知] 收到新訂單 #{}，已通知餐廳準備。", orderId);
        } else if ("PREPARING".equals(status)) {
            log.info("[通知] 訂單 #{} 製作中。", orderId);
        } else if ("COMPLETED".equals(status)) {
            log.info("[通知] 訂單 #{} 已完成，請前往櫃檯取餐。", orderId);
        } else {
            log.info("[通知] 訂單 #{} 狀態更新: {}", orderId, status);
        }
    }
}
