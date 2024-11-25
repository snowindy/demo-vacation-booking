package com.example.demo.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class VacationRequestDTO {
    private Long id;
    private Long authorId;
    private String status;
    private Long resolvedById;
    private Instant requestCreatedAt;
    private Instant vacationStartDate;
    private Instant vacationEndDate;
    private Instant resolutionDate;
    private String resolutionComment;
    private Instant createdAt;
    private Instant updatedAt;
} 