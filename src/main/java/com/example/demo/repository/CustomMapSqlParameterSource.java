package com.example.demo.repository;

import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;


import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CustomMapSqlParameterSource extends MapSqlParameterSource {
    public CustomMapSqlParameterSource(String paramName, @Nullable Object value) {
        super(paramName, value);
    }
    
    @Override
    @NonNull
    public Object getValue(String paramName) throws IllegalArgumentException {
        Object value = super.getValue(paramName);
        if (value instanceof Instant) {
            return Timestamp.from((Instant) value);
        }
        return value;
    }
}