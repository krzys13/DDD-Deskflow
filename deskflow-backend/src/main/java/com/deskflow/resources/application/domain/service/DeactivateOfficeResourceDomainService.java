package com.deskflow.resources.application.domain.service;

import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.model.vo.AllocationUsageStatus;

import java.util.List;

public class DeactivateOfficeResourceDomainService {

    public void deactivateOfficeResource(OfficeResource resource, List<AllocationUsageStatus> relatedRequestStatuses) {
        if (!canModifyResource(relatedRequestStatuses)) {
            throw new IllegalStateException("Cannot deactivate a resource with active allocation requests");
        }
        resource.deactivate();
    }

    private boolean canModifyResource(List<AllocationUsageStatus> relatedRequestStatuses) {
        return relatedRequestStatuses.stream()
                .noneMatch(status -> status == AllocationUsageStatus.IN_USE
                        || status == AllocationUsageStatus.APPROVED
                        || status == AllocationUsageStatus.REQUESTED
                        || status == AllocationUsageStatus.OVERDUE);
    }
}
