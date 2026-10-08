package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;

public class ApproveAllocationRequestDomainService {

    public void approveAllocationRequest(AllocationRequest request) {
        request.approve();
    }
}
