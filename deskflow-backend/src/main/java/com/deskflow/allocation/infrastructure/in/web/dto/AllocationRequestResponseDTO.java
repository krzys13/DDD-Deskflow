package com.deskflow.allocation.infrastructure.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AllocationRequestResponseDTO(
        UUID id,
        UUID employeeId,
        String employeeFirstName,
        String employeeLastName,
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
