package com.example.demo.repository;

import com.example.demo.model.VacationRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;
import java.lang.reflect.Field;
import java.time.Instant;

@Repository
public class VacationRequestRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<VacationRequest> rowMapper;

    public VacationRequestRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new BeanPropertyRowMapper<>(VacationRequest.class);
    }

    public Optional<VacationRequest> findById(Long id) {
        String sql = "SELECT * FROM vacation_requests WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<VacationRequest> findByAuthorId(Long authorId) {
        String sql = "SELECT * FROM vacation_requests WHERE author_id = :authorId ORDER BY request_created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("authorId", authorId);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public List<VacationRequest> findByAuthorIdAndStatus(Long authorId, VacationRequest.Status status) {
        String sql = "SELECT * FROM vacation_requests WHERE author_id = :authorId AND status = :status " +
                    "ORDER BY request_created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("authorId", authorId)
            .addValue("status", status.toString().toLowerCase());
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public List<VacationRequest> findAll() {
        String sql = "SELECT * FROM vacation_requests ORDER BY request_created_at DESC";
        return jdbcTemplate.query(sql, new MapSqlParameterSource(), rowMapper);
    }

    public List<VacationRequest> findByStatus(VacationRequest.Status status) {
        String sql = "SELECT * FROM vacation_requests WHERE status = :status " +
                    "ORDER BY request_created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("status", status.toString().toLowerCase());
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public List<VacationRequest> findOverlapping(Instant startDate, Instant endDate) {
        String sql = "SELECT * FROM vacation_requests " +
                    "WHERE status = 'approved'::request_status " +
                    "AND (vacation_start_date, vacation_end_date) OVERLAPS (:startDate, :endDate)";
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("startDate", startDate)
            .addValue("endDate", endDate);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public VacationRequest save(VacationRequest request) {
        if (request.getId() == null) {
            return insert(request);
        }
        return update(request);
    }

    private MapSqlParameterSource createParameterSource(Object object) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        try {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String paramName = camelToSnakeCase(field.getName());
                Object value = field.get(object);
                if (value instanceof VacationRequest.Status) {
                    value = value.toString().toLowerCase();
                }
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

    private VacationRequest insert(VacationRequest request) {
        String sql = "INSERT INTO vacation_requests (author_id, status, resolved_by_id, request_created_at, " +
                    "vacation_start_date, vacation_end_date, resolution_date, resolution_comment, " +
                    "created_at, updated_at) " +
                    "VALUES (:author_id, :status, :resolved_by_id, :request_created_at, " +
                    ":vacation_start_date, :vacation_end_date, :resolution_date, :resolution_comment, " +
                    "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                    "RETURNING *";
        
        return jdbcTemplate.queryForObject(sql, createParameterSource(request), rowMapper);
    }

    private VacationRequest update(VacationRequest request) {
        String sql = "UPDATE vacation_requests SET " +
                    "status = :status, resolved_by_id = :resolved_by_id, " +
                    "vacation_start_date = :vacation_start_date, vacation_end_date = :vacation_end_date, " +
                    "resolution_date = :resolution_date, resolution_comment = :resolution_comment, " +
                    "updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = :id RETURNING *";

        return jdbcTemplate.queryForObject(sql, createParameterSource(request), rowMapper);
    }
} 