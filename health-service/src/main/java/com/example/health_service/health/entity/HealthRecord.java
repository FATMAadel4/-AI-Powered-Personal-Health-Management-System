package com.example.health_service.health.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // مش Foreign Key — بس رقم بناخده من الـ JWT
    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type; // GLUCOSE, BLOOD_PRESSURE, PULSE...

    @Column(nullable = false)
    private Double value; // القراءة نفسها

    private String unit; // mg/dL, mmHg, bpm...

    private String notes; // ملاحظات اختيارية

    private String fileUrl; // لو رفع PDF أو صورة

    @CreationTimestamp
    private LocalDateTime recordedAt; // وقت التسجيل تلقائي
}
