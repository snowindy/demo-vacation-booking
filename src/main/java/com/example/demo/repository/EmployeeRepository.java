package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Optional;

@Repository
public class EmployeeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Employee> rowMapper;

    public EmployeeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new BeanPropertyRowMapper<>(Employee.class);
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

    private SqlParameterSource createParameterSource(Employee employee) {
        return new BeanPropertySqlParameterSource(employee) {
            @Override
            public Object getValue(String paramName) throws IllegalArgumentException {
                Object value = super.getValue(paramName);
                if (value instanceof Instant) {
                    return Timestamp.from((Instant) value);
                }
                return value;
            }
        };
    }

    private Employee insert(Employee employee) {
        String sql = "INSERT INTO employees (email, first_name, last_name, password_hash, role, manager_id, " +
                "vacation_days_total, vacation_days_used) VALUES (:email, :firstName, :lastName, :passwordHash, " +
                ":role, :managerId, :vacationDaysTotal, :vacationDaysUsed) RETURNING *";
        
        return jdbcTemplate.queryForObject(sql, createParameterSource(employee), rowMapper);
    }

    private Employee update(Employee employee) {
        String sql = "UPDATE employees SET email = :email, first_name = :firstName, last_name = :lastName, " +
                "role = :role, manager_id = :managerId, vacation_days_total = :vacationDaysTotal, " +
                "vacation_days_used = :vacationDaysUsed WHERE id = :id RETURNING *";

        return jdbcTemplate.queryForObject(sql, createParameterSource(employee), rowMapper);
    }
} 