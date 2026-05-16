package com.example.health_service.health.controller;
import com.example.health_service.health.Dto.RecordRequest;
import com.example.health_service.health.entity.RecordType;
import com.example.health_service.health.repository.HealthRecordRepository;
import com.example.health_service.health.Dto.RecordResponse;
import com.example.health_service.health.security.JwtFilter;
import com.example.health_service.health.service.FileStorageService;
import com.example.health_service.health.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;//
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private JwtFilter jwtFilter;

    // ============================================================
    // POST /api/health/records — أضف قراءة جديدة
    // ============================================================
    @PostMapping("/records")
    public ResponseEntity<RecordResponse> addRecord(
            @Valid @RequestBody RecordRequest req,
            HttpServletRequest request) {

        Long userId = jwtFilter.extractUserId(request);
        return ResponseEntity.ok(healthService.addRecord(userId, req));
    }

    // ============================================================
    // GET /api/health/records — جيب كل القراءات مع Pagination
    // ?page=0&size=10&type=GLUCOSE (type اختياري)
    // ============================================================
    @GetMapping("/records")
    public ResponseEntity<Page<RecordResponse>> getRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) RecordType type,
            HttpServletRequest request) {

        Long userId = jwtFilter.extractUserId(request);

        if (type != null) {
            return ResponseEntity.ok(
                    healthService.getRecordsByType(userId, type, page, size)
            );
        }

        return ResponseEntity.ok(healthService.getRecords(userId, page, size));
    }

    // ============================================================
    // GET /api/health/records/range — جيب بين تاريخين
    // ?from=2026-01-01T00:00:00&to=2026-05-15T23:59:59
    // ============================================================
    @GetMapping("/records/range")
    public ResponseEntity<List<RecordResponse>> getRecordsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            HttpServletRequest request) {

        Long userId = jwtFilter.extractUserId(request);
        return ResponseEntity.ok(healthService.getRecordsByDateRange(userId, from, to));
    }

    // ============================================================
    // GET /api/health/records/stats — إحصائيات
    // ?type=GLUCOSE
    // ============================================================
    @GetMapping("/records/stats")
    public ResponseEntity<?> getStats(
            @RequestParam RecordType type,
            HttpServletRequest request) {

        Long userId = jwtFilter.extractUserId(request);

        return ResponseEntity.ok(
                java.util.Map.of(
                        "max", healthService.getMaxValue(userId, type),
                        "min", healthService.getMinValue(userId, type),
                        "type", type
                )
        );
    }

    // ============================================================
    // PUT /api/health/records/{id} — عدّل قراءة
    // ============================================================
    @PutMapping("/records/{id}")
    public ResponseEntity<RecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody RecordRequest req,
            HttpServletRequest request) {

        Long userId = jwtFilter.extractUserId(request);
        return ResponseEntity.ok(healthService.updateRecord(userId, id, req));
    }

    // ============================================================
    // DELETE /api/health/records/{id} — امسح قراءة
    // ============================================================
    @DeleteMapping("/records/{id}")
    public ResponseEntity<String> deleteRecord(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long userId = jwtFilter.extractUserId(request);
        healthService.deleteRecord(userId, id);
        return ResponseEntity.ok("Record deleted successfully");
    }

    // ============================================================
    // POST /api/health/files/upload — ارفع PDF أو صورة
    // ============================================================

    @Operation(summary = "Upload file")
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestPart("file") MultipartFile file,   //  @RequestPart بدل @RequestParam
            HttpServletRequest request) throws IOException {

        Long userId = jwtFilter.extractUserId(request);
        String fileUrl = fileStorageService.saveFile(userId, file);
        return ResponseEntity.ok(fileUrl);
    }
}

