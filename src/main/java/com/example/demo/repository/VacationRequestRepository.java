package com.example.demo.repository;

import com.example.demo.model.VacationRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.sql.Timestamp;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class VacationRequestRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private BeanPropertyRowMapper<VacationRequest> rowMapper = new BeanPropertyRowMapper<>(VacationRequest.class);

    {
        var cs = new DefaultConversionService();
        cs.addConverter(String.class, 
        VacationRequest.Status.class, VacationRequest.Status::valueOf);
        rowMapper.setConversionService(cs);
    }

    public Optional<VacationRequest> findById(Long id) {
        String sql = "SELECT * FROM vacation_requests WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, rowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<VacationRequest> findByAuthorId(Long authorId) {
        String sql = "SELECT * FROM vacation_requests WHERE author_id = :authorId ORDER BY request_created_at DESC";
        var params = new MapSqlParameterSource("authorId", authorId);
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public List<VacationRequest> findByAuthorIdAndStatus(Long authorId, VacationRequest.Status status) {
        String sql = "SELECT * FROM vacation_requests WHERE author_id = :authorId AND status = CAST(:status AS request_status) " +
                    "ORDER BY request_created_at DESC";
        var params = new MapSqlParameterSource()
            .addValue("authorId", authorId)
            .addValue("status", status.toString());
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public List<VacationRequest> findAll() {
        String sql = "SELECT * FROM vacation_requests ORDER BY request_created_at DESC";
        return jdbcTemplate.query(sql, new CustomMapSqlParameterSource(), rowMapper);
    }

    public List<VacationRequest> findByStatus(VacationRequest.Status status) {
        String sql = "SELECT * FROM vacation_requests WHERE status = CAST(:status AS request_status) " +
                    "ORDER BY request_created_at DESC";
        var params = new CustomMapSqlParameterSource("status", status.toString());
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public List<VacationRequest> findOverlapping(Instant startDate, Instant endDate) {
        String sql = "SELECT * FROM vacation_requests " +
                    "WHERE status <> 'REJECTED'::request_status " +
                    "AND (vacation_start_date, vacation_end_date) OVERLAPS (:startDate, :endDate)";
        var params = new CustomMapSqlParameterSource()
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
    

    private SqlParameterSource createParameterSource(VacationRequest employee) {
        return new BeanPropertySqlParameterSource(employee) {
            @Override
            @NonNull
            public Object getValue(String paramName) throws IllegalArgumentException {
                Object value = super.getValue(paramName);
                if (value instanceof Instant) {
                    return Timestamp.from((Instant) value);
                }
                if (value instanceof VacationRequest.Status) {
                    return value.toString();
                }
                return value;
            }

            @Override
            public int getSqlType(String paramName) {
                Object value = getValue(paramName);
                if (value instanceof Timestamp) {
                    return java.sql.Types.TIMESTAMP;
                }
                return super.getSqlType(paramName);
            }
        };
    }

    private VacationRequest insert(VacationRequest request) {
        String sql = "INSERT INTO vacation_requests (author_id, status, resolved_by_id, request_created_at, " +
                    "vacation_start_date, vacation_end_date, resolution_date, resolution_comment) " +
                    "VALUES (:authorId, CAST(:status AS request_status), :resolvedById, :requestCreatedAt, " +
                    ":vacationStartDate, :vacationEndDate, :resolutionDate, :resolutionComment) " +
                    "RETURNING *";
        
        return jdbcTemplate.queryForObject(sql, createParameterSource(request), rowMapper);
    }

    private VacationRequest update(VacationRequest request) {
        String sql = "UPDATE vacation_requests SET " +
                    "status = CAST(:status AS request_status), resolved_by_id = :resolvedById, " +
                    "vacation_start_date = :vacationStartDate, vacation_end_date = :vacationEndDate, " +
                    "resolution_date = :resolutionDate, resolution_comment = :resolutionComment " +
                    "WHERE id = :id RETURNING *";

        return jdbcTemplate.queryForObject(sql, createParameterSource(request), rowMapper);
    }
} 