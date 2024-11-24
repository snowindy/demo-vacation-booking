package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacationRequest {
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    private Long id;
    private Long authorId;
    private Status status;
    private Long resolvedById;
    private Instant requestCreatedAt;
    private Instant vacationStartDate;
    private Instant vacationEndDate;
    private Instant resolutionDate;
    private String resolutionComment;
    private Instant createdAt;
    private Instant updatedAt;
} 