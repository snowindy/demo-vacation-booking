package com.example.demo.repository;

import com.example.demo.model.Employee;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.util.Optional;
import java.lang.reflect.Field;

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

    private MapSqlParameterSource createParameterSource(Object object) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String paramName = camelToSnakeCase(field.getName());
                Object value = field.get(object);
                params.addValue(paramName, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error creating parameter source", e);
        }
        return params;
    }

    private String camelToSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private Employee insert(Employee employee) {
        String sql = "INSERT INTO employees (email, first_name, last_name, password_hash, role, manager_id, " +
                "vacation_days_total, vacation_days_used) VALUES (:email, :first_name, :last_name, :password_hash, " +
                ":role, :manager_id, :vacation_days_total, :vacation_days_used) RETURNING *";
        
        return jdbcTemplate.queryForObject(sql, createParameterSource(employee), rowMapper);
    }

    private Employee update(Employee employee) {
        String sql = "UPDATE employees SET email = :email, first_name = :first_name, last_name = :last_name, " +
                "role = :role, manager_id = :manager_id, vacation_days_total = :vacation_days_total, " +
                "vacation_days_used = :vacation_days_used WHERE id = :id RETURNING *";

        return jdbcTemplate.queryForObject(sql, createParameterSource(employee), rowMapper);
    }
} 