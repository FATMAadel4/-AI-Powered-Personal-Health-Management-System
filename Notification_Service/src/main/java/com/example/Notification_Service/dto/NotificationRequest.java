package com.example.Notification_Service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private String type = "INFO"; // INFO, WARNING, SUCCESS
}
