package com.example.health_service.health.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final String BASE_DIR = "uploads";

    public String saveFile(Long userId, MultipartFile file) throws IOException {

        // 1. اعمل فولدر لليوزر ده لو مش موجود
        String userDir = BASE_DIR + "/user-" + userId;
        Files.createDirectories(Paths.get(userDir));

        // 2. اعمل اسم مميز للملف — وقت الرفع + اسم الملف الأصلي
        String fileName = System.currentTimeMillis()
                + "-" + file.getOriginalFilename();

        // 3. احفظ الملف
        Path filePath = Paths.get(userDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        // 4. ارجّع الـ URL
        return userDir + "/" + fileName;
    }

    public void deleteFile(String fileUrl) throws IOException {
        Path filePath = Paths.get(fileUrl);
        Files.deleteIfExists(filePath);
    }
}

