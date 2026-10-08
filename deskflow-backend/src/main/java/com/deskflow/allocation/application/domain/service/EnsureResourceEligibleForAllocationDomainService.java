package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;

public class EnsureResourceEligibleForAllocationDomainService {

    public void ensureResourceEligibleForAllocation(ResourceAllocationProfile resource) {
        if (!resource.isActive() || !resource.isAvailable()) {
            throw new IllegalStateException("Resource is not available for allocation");
        }
    }
}
