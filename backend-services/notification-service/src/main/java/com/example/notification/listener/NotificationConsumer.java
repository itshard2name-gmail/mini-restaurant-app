package com.example.notification.listener;

import com.example.notification.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void receiveMessage(Map<String, Object> message) {
        System.out.println("Received order notification: " + message);

        // Broadcast to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/orders", message);
    }
}
