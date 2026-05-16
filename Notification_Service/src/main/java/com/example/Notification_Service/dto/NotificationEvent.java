package com.example.Notification_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String message;
    private String type;        // INFO, WARNING, SUCCESS
    private LocalDateTime sentAt;
}

