package com.example.ai_service.controller;

import com.example.ai_service.dto.ChatRequest;
import com.example.ai_service.dto.ChatResponse;
import com.example.ai_service.security.JwtFilter;
import com.example.ai_service.service.AiFacade;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    @Autowired
    private AiFacade aiFacade;

    @Autowired
    private JwtFilter jwtFilter;

    // ============================================================
    // POST /api/ai/chat — محادثة عامة
    // Body: { "message": "what is diabetes?" }
    // ============================================================
    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {

        jwtFilter.extractUserId(httpRequest);
        return ResponseEntity.ok(aiFacade.handleChat(request.getMessage()));
    }

    // ============================================================
    // POST /api/ai/ask-reports — سؤال على التقارير
    // Body: { "message": "what does the report say?" }
    // ============================================================
    @PostMapping(
            value = "/ask-reports",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChatResponse> askReports(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {

        jwtFilter.extractUserId(httpRequest);
        return ResponseEntity.ok(aiFacade.handleReportQuery(request.getMessage()));
    }

    // ============================================================
    // POST /api/ai/index — رفع PDF
    // Form: file = [اختاري ملف PDF]
    // ============================================================
    @PostMapping(
            value = "/index",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ChatResponse> indexPdf(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest httpRequest) throws Exception {

        jwtFilter.extractUserId(httpRequest);

        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Only PDF files are allowed"));
        }

        return ResponseEntity.ok(aiFacade.handlePdfIndex(file));
    }

    // ============================================================
    // POST /api/ai/analyze — تحليل صورة أشعة
    // Form: image = [اختاري صورة]
    // ============================================================
    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ChatResponse> analyzeImage(
            @RequestPart("image") MultipartFile image,
            HttpServletRequest httpRequest) throws Exception {

        jwtFilter.extractUserId(httpRequest);

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Only image files are allowed"));
        }

        return ResponseEntity.ok(aiFacade.handleImageAnalysis(image));
    }
}