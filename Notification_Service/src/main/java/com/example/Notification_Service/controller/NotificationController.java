package com.example.Notification_Service.controller;
import com.example.Notification_Service.dto.EmailRequest;
import com.example.Notification_Service.dto.NotificationRequest;
import com.example.Notification_Service.security.JwtFilter;
import com.example.Notification_Service.service.EmailService;
import com.example.Notification_Service.service.WebSocketService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private JwtFilter jwtFilter;

    // ============================================================
    // POST /api/notify/email — بعت إيميل
    // ============================================================
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(
            @Valid @RequestBody EmailRequest request,
            HttpServletRequest httpRequest) {

        jwtFilter.extractUserId(httpRequest);

        try {
            emailService.sendEmail(request);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    // ============================================================
    // POST /api/notify/send — إشعار real-time لنفس اليوزر
    // ============================================================
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @Valid @RequestBody NotificationRequest request,
            HttpServletRequest httpRequest) {

        Long userId = jwtFilter.extractUserId(httpRequest);
        webSocketService.notifyUser(userId, request.getMessage(), request.getType());
        return ResponseEntity.ok("Notification sent");
    }

    // ============================================================
    // POST /api/notify/medication — تذكير دواء
    // ============================================================
    @PostMapping("/medication")
    public ResponseEntity<String> sendMedicationReminder(
            @RequestParam String email,
            @RequestParam String medication,
            @RequestParam String time,
            HttpServletRequest httpRequest) {

        jwtFilter.extractUserId(httpRequest);

        try {
            emailService.sendMedicationReminder(email, medication, time);
            return ResponseEntity.ok("Medication reminder sent to " + email);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send reminder: " + e.getMessage());
        }
    }

    // ============================================================
    // POST /api/notify/alert — تنبيه قراءة عالية
    // ============================================================
    @PostMapping("/alert")
    public ResponseEntity<String> sendHighReadingAlert(
            @RequestParam String email,
            @RequestParam String type,
            @RequestParam Double value,
            @RequestParam String unit,
            HttpServletRequest httpRequest) {

        jwtFilter.extractUserId(httpRequest);

        try {
            emailService.sendHighReadingAlert(email, type, value, unit);
            return ResponseEntity.ok("Alert sent to " + email);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send alert: " + e.getMessage());
        }
    }
}
