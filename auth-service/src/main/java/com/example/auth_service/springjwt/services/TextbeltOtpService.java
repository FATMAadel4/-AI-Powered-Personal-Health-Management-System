package com.example.auth_service.springjwt.services;

import com.example.auth_service.springjwt.entity.Otp;
import com.example.auth_service.springjwt.repositories.OtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class TextbeltOtpService {

    private final JavaMailSender mailSender;
    private final OtpRepository otpRepository; // ← جديد

    @Value("${spring.mail.username}")
    private String fromEmail;

    public TextbeltOtpService(JavaMailSender mailSender, OtpRepository otpRepository) {
        this.mailSender = mailSender;
        this.otpRepository = otpRepository;
    }

    public String sendOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // احفظ في الـ DB بدل HashMap
        otpRepository.deleteByEmail(email); // امسح القديم لو موجود
        Otp otpEntity = Otp.builder()
                .email(email)
                .otp(otp)
                .expirationTime(LocalDateTime.now().plusMinutes(5))
                .build();
        otpRepository.save(otpEntity);

        // ابعت الإيميل
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
        mailSender.send(message);

        return otp;
    }

    public boolean verifyOtp(String email, String code) {
        Optional<Otp> otpOpt = otpRepository.findByEmail(email);

        if (otpOpt.isPresent()) {
            Otp otpEntity = otpOpt.get();

            // تحقق من الكود والوقت
            if (otpEntity.getOtp().equals(code) &&
                    otpEntity.getExpirationTime().isAfter(LocalDateTime.now())) {
                otpRepository.deleteByEmail(email); // امسح بعد الاستخدام
                return true;
            }
        }
        return false;
    }
}