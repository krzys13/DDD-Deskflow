package com.deskflow.resources.infrastructure.out.allocation;

import com.deskflow.allocation.application.port.in.AllocationRequestInputPort;
import com.deskflow.resources.application.port.out.ResourceAllocationRequestPort;
import com.deskflow.resources.application.domain.model.vo.AllocationUsageStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllocationRequestAdapter implements ResourceAllocationRequestPort {

    private final AllocationRequestInputPort allocationRequestInputPort;

    public AllocationRequestAdapter(@Lazy AllocationRequestInputPort allocationRequestInputPort) {
        this.allocationRequestInputPort = allocationRequestInputPort;
    }

    @Override
    public List<AllocationUsageStatus> findStatusesForResource(ResourceId resourceId) {
        return allocationRequestInputPort.findActiveForResource(resourceId).stream()
                .map(request -> request.status())
                .map(this::toUsageStatus)
                .toList();
    }

    private AllocationUsageStatus toUsageStatus(String status) {
        return switch (status) {
            case "SUBMITTED" -> AllocationUsageStatus.REQUESTED;
            case "APPROVED" -> AllocationUsageStatus.APPROVED;
            case "ACTIVE" -> AllocationUsageStatus.IN_USE;
            case "OVERDUE" -> AllocationUsageStatus.OVERDUE;
            case "REJECTED", "CANCELLED", "COMPLETED" -> AllocationUsageStatus.CLOSED;
            default -> throw new IllegalArgumentException("Unknown allocation status: " + status);
        };
    }
}
