package com.example.demo.dto;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class EmployeeDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long managerId;
    private Integer vacationDaysTotal;
    private Integer vacationDaysUsed;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
} 