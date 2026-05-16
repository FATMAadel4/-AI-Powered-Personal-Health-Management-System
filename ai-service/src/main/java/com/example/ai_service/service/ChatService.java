package com.example.ai_service.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    private ChatLanguageModel chatModel;

    // ============================================================
    // محادثة عامة عن الصحة
    // ============================================================
    public String chat(String message) {
        String systemPrompt =
                "You are a helpful health assistant. " +
                        "Answer health-related questions clearly and simply. " +
                        "Always recommend consulting a doctor for serious medical concerns. " +
                        "Support both Arabic and English languages.";

        return chatModel.generate(systemPrompt + "\n\nUser: " + message);
    }
}