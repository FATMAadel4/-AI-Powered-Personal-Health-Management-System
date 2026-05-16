package com.example.Notification_Service.service;

import com.example.Notification_Service.dto.NotificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ============================================================
    // بعت إشعار لـ user معين
    // Client بيـ subscribe على /topic/user/{userId}
    // ============================================================
    public void notifyUser(Long userId, String message, String type) {
        NotificationEvent event = new NotificationEvent(
                message,
                type,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/user/" + userId,
                event
        );
    }

    // ============================================================
    // بعت إشعار لكل الـ users
    // ============================================================
    public void notifyAll(String message, String type) {
        NotificationEvent event = new NotificationEvent(
                message,
                type,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/all", event);
    }
}









