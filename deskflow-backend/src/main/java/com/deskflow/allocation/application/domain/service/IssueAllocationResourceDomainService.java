package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;

public class IssueAllocationResourceDomainService {

    public void issueAllocationResource(AllocationRequest request) {
        request.issueResource();
    }
}
