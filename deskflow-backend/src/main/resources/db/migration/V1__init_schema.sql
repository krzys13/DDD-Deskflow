CREATE TABLE employees (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE allocation_limits (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employees(id),
    resource_type VARCHAR(50) NOT NULL,
    limit_amount INT NOT NULL DEFAULT 1,
    UNIQUE (employee_id, resource_type)
);

CREATE TABLE office_resources (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    building VARCHAR(100) NOT NULL,
    floor INT NOT NULL,
    number VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE allocation_requests (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employees(id),
    resource_id UUID NOT NULL REFERENCES office_resources(id),
    status VARCHAR(50) NOT NULL,
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    justification TEXT,
    submitted_at TIMESTAMP NOT NULL,
    issued_at TIMESTAMP,
    returned_at TIMESTAMP,
    rejection_reason TEXT
);

CREATE INDEX idx_allocation_requests_status ON allocation_requests(status);
CREATE INDEX idx_allocation_requests_employee_id ON allocation_requests(employee_id);
CREATE INDEX idx_allocation_requests_resource_id ON allocation_requests(resource_id);
CREATE INDEX idx_allocation_requests_date_to ON allocation_requests(date_to);
