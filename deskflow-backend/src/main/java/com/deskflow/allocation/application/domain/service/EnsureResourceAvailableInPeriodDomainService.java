package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;

import java.util.List;

public class EnsureResourceAvailableInPeriodDomainService {

    public void ensureResourceAvailableInPeriod(List<AllocationRequest> overlappingRequests) {
        if (!overlappingRequests.isEmpty()) {
            throw new IllegalStateException("Resource is not available for the requested period");
        }
    }
}
