package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Optional;

@Repository
public class EmployeeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Employee> rowMapper;

    public EmployeeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = (rs, rowNum) -> Employee.builder()
                .id(rs.getLong("id"))
                .email(rs.getString("email"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .role(rs.getString("role"))
                .managerId(rs.getObject("manager_id", Long.class))
                .vacationDaysTotal(rs.getInt("vacation_days_total"))
                .vacationDaysUsed(rs.getInt("vacation_days_used"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
    }

    public Optional<Employee> findById(Long id) {
        String sql = "SELECT * FROM employees WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            return insert(employee);
        }
        return update(employee);
    }

    private Employee insert(Employee employee) {
        String sql = "INSERT INTO employees (email, first_name, last_name, password_hash, role, manager_id, " +
                "vacation_days_total, vacation_days_used) VALUES (:email, :firstName, :lastName, :passwordHash, " +
                ":role, :managerId, :vacationDaysTotal, :vacationDaysUsed) RETURNING *";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", employee.getEmail())
                .addValue("firstName", employee.getFirstName())
                .addValue("lastName", employee.getLastName())
                .addValue("passwordHash", employee.getPasswordHash())
                .addValue("role", employee.getRole())
                .addValue("managerId", employee.getManagerId())
                .addValue("vacationDaysTotal", employee.getVacationDaysTotal())
                .addValue("vacationDaysUsed", employee.getVacationDaysUsed());

        return jdbcTemplate.queryForObject(sql, params, rowMapper);
    }

    private Employee update(Employee employee) {
        String sql = "UPDATE employees SET email = :email, first_name = :firstName, last_name = :lastName, " +
                "role = :role, manager_id = :managerId, vacation_days_total = :vacationDaysTotal, " +
                "vacation_days_used = :vacationDaysUsed WHERE id = :id RETURNING *";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", employee.getId())
                .addValue("email", employee.getEmail())
                .addValue("firstName", employee.getFirstName())
                .addValue("lastName", employee.getLastName())
                .addValue("role", employee.getRole())
                .addValue("managerId", employee.getManagerId())
                .addValue("vacationDaysTotal", employee.getVacationDaysTotal())
                .addValue("vacationDaysUsed", employee.getVacationDaysUsed());

        return jdbcTemplate.queryForObject(sql, params, rowMapper);
    }
} 