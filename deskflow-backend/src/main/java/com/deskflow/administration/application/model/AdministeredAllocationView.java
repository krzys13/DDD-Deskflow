package com.deskflow.administration.application.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdministeredAllocationView(
        UUID id,
        UUID employeeId,
        UUID resourceId,
        String status,
        LocalDate dateFrom,
        LocalDate dateTo,
        String justification,
        LocalDateTime submittedAt,
        LocalDateTime issuedAt,
        LocalDateTime returnedAt,
        String rejectionReason
) {}
