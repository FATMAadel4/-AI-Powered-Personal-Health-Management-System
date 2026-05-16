package com.example.ai_service.service;

import com.example.ai_service.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// ============================================================
// AiFacade — بيوزّع الطلبات على الـ services الصح
// نفس الفكرة اللي عملتيها في Animal Chatbot 😊
// ============================================================
@Service
public class AiFacade {

    @Autowired
    private RagService ragService;

    @Autowired
    private VisionService visionService;

    @Autowired
    private ChatService chatService;

    // محادثة عامة
    public ChatResponse handleChat(String message) {
        String answer = chatService.chat(message);
        return new ChatResponse(answer);
    }

    // سؤال على التقارير
    public ChatResponse handleReportQuery(String question) {
        String answer = ragService.askAboutReports(question);
        return new ChatResponse(answer);
    }

    // رفع PDF للـ indexing
    public ChatResponse handlePdfIndex(MultipartFile file) throws IOException {
        String result = ragService.indexPdf(file);
        return new ChatResponse(result);
    }

    // تحليل صورة
    public ChatResponse handleImageAnalysis(MultipartFile image) throws IOException {
        String analysis = visionService.analyzeMedicalImage(image);
        return new ChatResponse(analysis);
    }
}
