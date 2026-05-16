package com.example.Notification_Service.service;

import com.example.Notification_Service.dto.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ============================================================
    // بعت إيميل عادي
    // ============================================================
    public void sendEmail(EmailRequest request) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(request.getTo());
        helper.setSubject(request.getSubject());
        helper.setText(request.getBody(), true); // true = HTML

        mailSender.send(message);
    }

    // ============================================================
    // بعت إيميل تذكير دواء
    // ============================================================
    public void sendMedicationReminder(String toEmail, String medicationName, String time) throws Exception {

        EmailRequest request = new EmailRequest();
        request.setTo(toEmail);
        request.setSubject("💊 Medication Reminder — HealthTrack");
        request.setBody(
                "<div style='font-family: Arial; padding: 20px;'>" +
                        "<h2 style='color: #2196F3;'>Medication Reminder</h2>" +
                        "<p>Don't forget to take your medication:</p>" +
                        "<h3 style='color: #4CAF50;'>" + medicationName + "</h3>" +
                        "<p>Scheduled time: <strong>" + time + "</strong></p>" +
                        "<hr>" +
                        "<p style='color: gray; font-size: 12px;'>HealthTrack Pro — Your Health Assistant</p>" +
                        "</div>"
        );

        sendEmail(request);
    }

    // ============================================================
    // بعت إيميل تنبيه قراءة عالية
    // ============================================================
    public void sendHighReadingAlert(String toEmail, String type, Double value, String unit) throws Exception {

        EmailRequest request = new EmailRequest();
        request.setTo(toEmail);
        request.setSubject("⚠️ High Reading Alert — HealthTrack");
        request.setBody(
                "<div style='font-family: Arial; padding: 20px;'>" +
                        "<h2 style='color: #F44336;'>⚠️ High Reading Alert</h2>" +
                        "<p>A high reading was recorded:</p>" +
                        "<h3 style='color: #F44336;'>" + type + ": " + value + " " + unit + "</h3>" +
                        "<p>Please consult your doctor if this reading is unusual.</p>" +
                        "<hr>" +
                        "<p style='color: gray; font-size: 12px;'>HealthTrack Pro — Your Health Assistant</p>" +
                        "</div>"
        );

        sendEmail(request);
    }
}

