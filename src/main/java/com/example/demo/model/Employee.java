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
public class Employee {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private String role;
    private Long managerId;
    private Integer vacationDaysTotal;
    private Integer vacationDaysUsed;
    private Instant createdAt;
    private Instant updatedAt;
} 