package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.EmployeeAllocationProfile;

public class EnsureEmployeeWithinLimitDomainService {

    public void ensureEmployeeWithinLimit(EmployeeAllocationProfile employee, String resourceType, long currentActiveForType) {
        int limit = employee.limitForType(resourceType);
        if (currentActiveForType >= limit) {
            throw new IllegalStateException("Employee has reached the allocation limit for this resource type");
        }
    }
}
