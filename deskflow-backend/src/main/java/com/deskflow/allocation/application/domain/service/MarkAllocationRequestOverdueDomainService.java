package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;

public class MarkAllocationRequestOverdueDomainService {

    public void markAllocationRequestOverdue(AllocationRequest request) {
        request.markAsOverdue();
    }
}
