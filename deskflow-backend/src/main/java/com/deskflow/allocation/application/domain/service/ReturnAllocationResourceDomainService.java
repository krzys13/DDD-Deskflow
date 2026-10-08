package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;

public class ReturnAllocationResourceDomainService {

    public void returnAllocationResource(AllocationRequest request) {
        request.returnResource();
    }
}
