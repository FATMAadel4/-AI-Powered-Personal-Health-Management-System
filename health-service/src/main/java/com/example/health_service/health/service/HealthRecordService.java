package com.example.health_service.health.service;
import com.example.health_service.health.repository.HealthRecordRepository;
import com.example.health_service.health.Dto.RecordRequest;
import com.example.health_service.health.Dto.RecordResponse;
import com.example.health_service.health.entity.HealthRecord;
import com.example.health_service.health.entity.RecordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthRecordService {

    @Autowired
    private HealthRecordRepository repo;

    // ============================================================
    // جيب كل records اليوزر — مع Cache
    // ============================================================
    @Cacheable(value = "records", key = "#userId + '_' + #page + '_' + #size")
    public Page<RecordResponse> getRecords(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("recordedAt").descending() // الأحدث الأول
        );

        return repo.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    // ============================================================
    // جيب بالنوع — مع Cache
    // ============================================================
    @Cacheable(value = "records", key = "#userId + '_' + #type + '_' + #page")
    public Page<RecordResponse> getRecordsByType(
            Long userId, RecordType type, int page, int size) {

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("recordedAt").descending()
        );

        return repo.findByUserIdAndType(userId, type, pageable)
                .map(this::toResponse);
    }

    // ============================================================
    // جيب بين تاريخين
    // ============================================================
    public List<RecordResponse> getRecordsByDateRange(
            Long userId, LocalDateTime from, LocalDateTime to) {

        return repo.findByDateRange(userId, from, to)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // أضف record جديد — مسح الـ Cache القديم
    // ============================================================
    @CacheEvict(value = "records", allEntries = true)
    public RecordResponse addRecord(Long userId, RecordRequest req) {

        HealthRecord record = HealthRecord.builder()
                .userId(userId)
                .type(req.getType())
                .value(req.getValue())
                .unit(req.getUnit())
                .notes(req.getNotes())
                .fileUrl(req.getFileUrl())
                .build();

        return toResponse(repo.save(record));
    }

    // ============================================================
    // عدّل record — مسح الـ Cache
    // ============================================================
    @CacheEvict(value = "records", allEntries = true)
    public RecordResponse updateRecord(Long userId, Long recordId, RecordRequest req) {

        HealthRecord record = repo.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // تأكد إن الـ record ده بتاع اليوزر ده فعلاً
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        record.setType(req.getType());
        record.setValue(req.getValue());
        record.setUnit(req.getUnit());
        record.setNotes(req.getNotes());

        return toResponse(repo.save(record));
    }

    // ============================================================
    // امسح record — مسح الـ Cache
    // ============================================================
    @CacheEvict(value = "records", allEntries = true)
    public void deleteRecord(Long userId, Long recordId) {

        HealthRecord record = repo.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        // تأكد إن الـ record ده بتاع اليوزر ده فعلاً
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        repo.delete(record);
    }

    // ============================================================
    // جيب إحصائيات
    // ============================================================
    public Double getMaxValue(Long userId, RecordType type) {
        return repo.findMaxValue(userId, type);
    }

    public Double getMinValue(Long userId, RecordType type) {
        return repo.findMinValue(userId, type);
    }

    // ============================================================
    // Mapper — من Entity لـ Response
    // ============================================================
    private RecordResponse toResponse(HealthRecord record) {
        return RecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .type(record.getType())
                .value(record.getValue())
                .unit(record.getUnit())
                .notes(record.getNotes())
                .fileUrl(record.getFileUrl())
                .recordedAt(record.getRecordedAt())
                .build();
    }
}

