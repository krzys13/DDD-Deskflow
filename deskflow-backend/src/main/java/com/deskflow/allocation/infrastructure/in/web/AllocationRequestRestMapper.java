package com.deskflow.allocation.infrastructure.in.web;

import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.infrastructure.in.web.dto.AllocationRequestResponseDTO;

public class AllocationRequestRestMapper {

    public static AllocationRequestResponseDTO toResponse(AllocationRequestView r) {
        return toResponse(r, null, null);
    }

    public static AllocationRequestResponseDTO toResponse(AllocationRequestView r, String firstName, String lastName) {
        return new AllocationRequestResponseDTO(
                r.id(),
                r.employeeId(),
                firstName,
                lastName,
                r.resourceId(),
                r.status(),
                r.dateFrom(),
                r.dateTo(),
                r.justification(),
                r.submittedAt(),
                r.issuedAt(),
                r.returnedAt(),
                r.rejectionReason()
        );
    }
}
