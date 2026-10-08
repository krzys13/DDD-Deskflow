package com.deskflow.allocation.application.domain.model;

import java.util.Map;

public record EmployeeAllocationProfile(Map<String, Integer> limitsByResourceType) {
    public int limitForType(String resourceType) {
        return limitsByResourceType.getOrDefault(resourceType, 1);
    }
}
