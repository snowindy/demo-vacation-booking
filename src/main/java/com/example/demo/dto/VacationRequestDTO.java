package com.example.demo.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class VacationRequestDTO {
    private Long id;
    private Long authorId;
    private String status;
    private Long resolvedById;
    private ZonedDateTime requestCreatedAt;
    private ZonedDateTime vacationStartDate;
    private ZonedDateTime vacationEndDate;
    private ZonedDateTime resolutionDate;
    private String resolutionComment;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
} 