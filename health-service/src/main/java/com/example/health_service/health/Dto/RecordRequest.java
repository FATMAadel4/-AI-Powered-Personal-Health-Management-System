package com.example.health_service.health.Dto;

import com.example.health_service.health.entity.RecordType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordRequest {

    @NotNull(message = "Type is required")
    private RecordType type;

    @NotNull(message = "Value is required")
    private Double value;

    private String unit;

    private String notes;

    private String fileUrl; // بيتملى بعد الـ upload
}
