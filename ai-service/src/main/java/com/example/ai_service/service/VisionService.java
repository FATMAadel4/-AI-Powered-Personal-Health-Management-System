package com.example.ai_service.service;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class VisionService {

    @Autowired
    private ChatLanguageModel chatModel;

    // ============================================================
    // اليوزر يرفع صورة أشعة أو تقرير — GPT يحللها
    // ============================================================
    public String analyzeMedicalImage(MultipartFile image) throws IOException {

        // 1. حوّل الصورة لـ Base64
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String mediaType = image.getContentType(); // image/jpeg أو image/png

        // 2. ابعت الصورة مع السؤال لـ GPT-4o-mini
        UserMessage message = UserMessage.from(
                ImageContent.from(base64Image, mediaType),
                TextContent.from(
                        "You are a medical imaging assistant. " +
                                "Please analyze this medical image and provide:\n" +
                                "1. What type of image this is (X-ray, MRI, etc.)\n" +
                                "2. Any visible findings or abnormalities\n" +
                                "3. General observations\n\n" +
                                "Important: Always recommend consulting a qualified doctor " +
                                "for proper diagnosis and treatment."
                )
        );

        // 3. رجّع التحليل
        return chatModel.generate(message).content().text();
    }
}
