package com.deskflow.allocation.infrastructure.out.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "allocation_requests")
class AllocationRequestEntity {

    @Id
    UUID id;

    @Column(name = "employee_id", nullable = false)
    UUID employeeId;

    @Column(name = "resource_id", nullable = false)
    UUID resourceId;

    @Column(nullable = false)
    String status;

    @Column(name = "date_from", nullable = false)
    LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    LocalDate dateTo;

    @Column(name = "justification")
    String justification;

    @Column(name = "submitted_at", nullable = false)
    LocalDateTime submittedAt;

    @Column(name = "issued_at")
    LocalDateTime issuedAt;

    @Column(name = "returned_at")
    LocalDateTime returnedAt;

    @Column(name = "rejection_reason")
    String rejectionReason;
}
