package com.deskflow.resources.application.port.out;

import com.deskflow.resources.application.domain.model.vo.AllocationUsageStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.util.List;

public interface ResourceAllocationRequestPort {
    List<AllocationUsageStatus> findStatusesForResource(ResourceId resourceId);
}
