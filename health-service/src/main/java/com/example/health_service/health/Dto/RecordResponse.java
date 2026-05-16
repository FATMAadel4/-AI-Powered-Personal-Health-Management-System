package com.example.health_service.health.Dto;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.example.health_service.health.entity.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor


public class RecordResponse {

    private Long id;
    private Long userId;
    private RecordType type;
    private Double value;
    private String unit;
    private String notes;
    private String fileUrl;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedAt;
}