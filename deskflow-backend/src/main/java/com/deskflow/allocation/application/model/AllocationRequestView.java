package com.deskflow.allocation.application.model;

import com.deskflow.allocation.application.domain.model.AllocationRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AllocationRequestView(
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
) {
    public static AllocationRequestView from(AllocationRequest request) {
        return new AllocationRequestView(
                request.getId().getValue(),
                request.getEmployeeId().getValue(),
                request.getResourceId().getValue(),
                request.getStatus().name(),
                request.getDateRange().getDateFrom(),
                request.getDateRange().getDateTo(),
                request.getJustification(),
                request.getSubmittedAt(),
                request.getIssuedAt(),
                request.getReturnedAt(),
                request.getRejectionReason()
        );
    }
}
