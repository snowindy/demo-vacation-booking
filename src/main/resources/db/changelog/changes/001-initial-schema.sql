--liquibase formatted sql

--changeset author:initial-schema-1 splitStatements:true
--comment: Create enum type for request status
CREATE TYPE request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

--changeset author:initial-schema-2 splitStatements:false
--comment: Create function for updating timestamp on record modification
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

--changeset author:initial-schema-3 splitStatements:true
--comment: Create employees table with basic user information and vacation tracking
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    manager_id BIGINT REFERENCES employees(id),
    vacation_days_total INTEGER NOT NULL DEFAULT 30,
    vacation_days_used INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

--changeset author:initial-schema-4 splitStatements:true
--comment: Create trigger for employees timestamp
CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON employees
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_timestamp();

--changeset author:initial-schema-5 splitStatements:true
--comment: Create vacation requests table for managing time off requests
CREATE TABLE vacation_requests (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT NOT NULL REFERENCES employees(id),
    status request_status NOT NULL DEFAULT 'PENDING',
    resolved_by_id BIGINT REFERENCES employees(id),
    request_created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    vacation_start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    vacation_end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    resolution_date TIMESTAMP WITH TIME ZONE,
    resolution_comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_date_range CHECK (vacation_end_date > vacation_start_date)
);

--changeset author:initial-schema-6 splitStatements:true
--comment: Create indexes for optimizing common queries
CREATE INDEX idx_vacation_requests_author ON vacation_requests(author_id);
CREATE INDEX idx_vacation_requests_status ON vacation_requests(status);
CREATE INDEX idx_vacation_requests_dates ON vacation_requests(vacation_start_date, vacation_end_date);
CREATE INDEX idx_employees_manager ON employees(manager_id);

--changeset author:initial-schema-7 splitStatements:true
--comment: Create trigger for vacation requests timestamp
CREATE TRIGGER set_timestamp
    BEFORE UPDATE ON vacation_requests
    FOR EACH ROW
    EXECUTE FUNCTION trigger_set_timestamp();

--rollback DROP TRIGGER IF EXISTS set_timestamp ON vacation_requests;
--rollback DROP TRIGGER IF EXISTS set_timestamp ON employees;
--rollback DROP FUNCTION IF EXISTS trigger_set_timestamp();
