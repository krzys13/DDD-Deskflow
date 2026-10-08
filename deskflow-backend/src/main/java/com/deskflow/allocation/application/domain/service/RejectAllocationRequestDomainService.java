package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;

public class RejectAllocationRequestDomainService {

    public void rejectAllocationRequest(AllocationRequest request, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be blank");
        }
        request.reject(reason);
    }
}
