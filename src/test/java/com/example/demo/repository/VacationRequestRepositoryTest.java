package com.example.demo.repository;

import com.example.demo.model.VacationRequest;
import com.example.demo.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.example.demo.config.TestDatabaseConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
    TestDatabaseConfig.class,
    EmployeeRepository.class,
    VacationRequestRepository.class
})
@Testcontainers
@ActiveProfiles("test")
@Transactional
class VacationRequestRepositoryTest {

    @Autowired
    private VacationRequestRepository vacationRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;
    private Employee testManager;
    private VacationRequest testRequest;


    @BeforeEach
    void setUp() {
        // Create test employee
        testEmployee = Employee.builder()
                .email("employee@example.com")
                .firstName("John")
                .lastName("Doe")
                .passwordHash("hashedPassword")
                .role("EMPLOYEE")
                .vacationDaysTotal(30)
                .vacationDaysUsed(0)
                .build();
        testEmployee = employeeRepository.save(testEmployee);

        // Create test manager
        testManager = Employee.builder()
                .email("manager@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .passwordHash("hashedPassword")
                .role("MANAGER")
                .vacationDaysTotal(30)
                .vacationDaysUsed(0)
                .build();
        testManager = employeeRepository.save(testManager);

        // Create test vacation request
        testRequest = VacationRequest.builder()
                .authorId(testEmployee.getId())
                .status(VacationRequest.Status.PENDING)
                .requestCreatedAt(Instant.now())
                .vacationStartDate(Instant.now().plus(10, ChronoUnit.DAYS))
                .vacationEndDate(Instant.now().plus(15, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void shouldSaveNewVacationRequest() {
        VacationRequest saved = vacationRequestRepository.save(testRequest);
        
        assertNotNull(saved.getId());
        assertEquals(testRequest.getAuthorId(), saved.getAuthorId());
        assertEquals(testRequest.getStatus(), saved.getStatus());
        assertNotNull(saved.getRequestCreatedAt());
    }

    @Test
    void shouldFindById() {
        VacationRequest saved = vacationRequestRepository.save(testRequest);
        Optional<VacationRequest> found = vacationRequestRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void shouldFindByAuthorId() {
        vacationRequestRepository.save(testRequest);
        List<VacationRequest> requests = vacationRequestRepository.findByAuthorId(testEmployee.getId());
        
        assertFalse(requests.isEmpty());
        assertEquals(testEmployee.getId(), requests.get(0).getAuthorId());
    }

    @Test
    void shouldFindByAuthorIdAndStatus() {
        vacationRequestRepository.save(testRequest);
        List<VacationRequest> requests = vacationRequestRepository.findByAuthorIdAndStatus(
            testEmployee.getId(), 
            VacationRequest.Status.PENDING
        );
        
        assertFalse(requests.isEmpty());
        assertEquals(VacationRequest.Status.PENDING, requests.get(0).getStatus());
    }
    @Test
    void shouldFindOverlappingRequests() {
        // Save initial request
        testRequest.setStatus(VacationRequest.Status.APPROVED);
        testRequest = vacationRequestRepository.save(testRequest);

        var testRequest1 = VacationRequest.builder()
        .authorId(testEmployee.getId())
        .status(VacationRequest.Status.REJECTED)
        .requestCreatedAt(Instant.now())
        .vacationStartDate(Instant.now().plus(10, ChronoUnit.DAYS))
        .vacationEndDate(Instant.now().plus(15, ChronoUnit.DAYS))
        .build();
        vacationRequestRepository.save(testRequest1);

        var testRequest2 = VacationRequest.builder()
        .authorId(testEmployee.getId())
        .status(VacationRequest.Status.REJECTED)
        .requestCreatedAt(Instant.now())
        .vacationStartDate(Instant.now().plus(20, ChronoUnit.DAYS))
        .vacationEndDate(Instant.now().plus(25, ChronoUnit.DAYS))
        .build();
        vacationRequestRepository.save(testRequest2);

        // Check for overlapping period
        Instant startDate = testRequest.getVacationStartDate().minus(1, ChronoUnit.DAYS);
        Instant endDate = testRequest.getVacationEndDate().plus(1, ChronoUnit.DAYS);
        
        List<VacationRequest> overlapping = vacationRequestRepository.findOverlapping(startDate, endDate);
        
        assertEquals(testRequest.getId(), overlapping.get(0).getId());
    }

    @Test
    void shouldUpdateExistingRequest() {
        // Save initial request
        testRequest.setStatus(VacationRequest.Status.APPROVED);
        testRequest = vacationRequestRepository.save(testRequest);

        // Update the request
        testRequest.setStatus(VacationRequest.Status.REJECTED);
        testRequest = vacationRequestRepository.save(testRequest);

        // Check if the request is updated
        Optional<VacationRequest> updatedRequest = vacationRequestRepository.findById(testRequest.getId());
        assertTrue(updatedRequest.isPresent());
        assertEquals(VacationRequest.Status.REJECTED, updatedRequest.get().getStatus());
    }
} 
