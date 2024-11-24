package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.config.TestDatabaseConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;



@SpringBootTest(classes = {
    TestDatabaseConfig.class,
    EmployeeRepository.class
})
@Testcontainers
@ActiveProfiles("test")
@Transactional
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(
            "SELECT * FROM employees", 
            Map.of()
        );
        System.out.println(results);
        testEmployee = Employee.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .passwordHash("hashedPassword")
                .role("EMPLOYEE")
                .vacationDaysTotal(30)
                .vacationDaysUsed(0)
                .build();
    }

    @Test
    void shouldSaveNewEmployee() {
        Employee saved = employeeRepository.save(testEmployee);
        
        assertNotNull(saved.getId());
        assertEquals(testEmployee.getEmail(), saved.getEmail());
        assertEquals(testEmployee.getFirstName(), saved.getFirstName());
        assertEquals(testEmployee.getLastName(), saved.getLastName());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void shouldFindEmployeeById() {
        Employee saved = employeeRepository.save(testEmployee);
        Optional<Employee> found = employeeRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getEmail(), found.get().getEmail());
    }

    @Test
    void shouldUpdateEmployee() {
        Employee saved = employeeRepository.save(testEmployee);
        saved.setFirstName("Jane");
        
        Employee updated = employeeRepository.save(saved);
        
        assertEquals("Jane", updated.getFirstName());
    }
} 