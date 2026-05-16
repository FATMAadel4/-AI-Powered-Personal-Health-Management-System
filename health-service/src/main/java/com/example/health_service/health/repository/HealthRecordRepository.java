package com.example.health_service.health.repository;

import com.example.health_service.health.entity.HealthRecord;
import com.example.health_service.health.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    // جيب كل records اليوزر — مع Pagination
    Page<HealthRecord> findByUserId(Long userId, Pageable pageable);

    // جيب بالنوع — مع Pagination
    Page<HealthRecord> findByUserIdAndType(
            Long userId,
            RecordType type,
            Pageable pageable
    );

    // جيب بين تاريخين — Custom Query
    @Query("SELECT r FROM HealthRecord r " +
            "WHERE r.userId = :userId " +
            "AND r.recordedAt BETWEEN :from AND :to " +
            "ORDER BY r.recordedAt DESC")
    List<HealthRecord> findByDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // جيب بالنوع وبين تاريخين
    @Query("SELECT r FROM HealthRecord r " +
            "WHERE r.userId = :userId " +
            "AND r.type = :type " +
            "AND r.recordedAt BETWEEN :from AND :to " +
            "ORDER BY r.recordedAt DESC")
    List<HealthRecord> findByTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") RecordType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // أعلى قراءة لنوع معين
    @Query("SELECT MAX(r.value) FROM HealthRecord r " +
            "WHERE r.userId = :userId AND r.type = :type")
    Double findMaxValue(
            @Param("userId") Long userId,
            @Param("type") RecordType type
    );

    // أقل قراءة لنوع معين
    @Query("SELECT MIN(r.value) FROM HealthRecord r " +
            "WHERE r.userId = :userId AND r.type = :type")
    Double findMinValue(
            @Param("userId") Long userId,
            @Param("type") RecordType type
    );
}
