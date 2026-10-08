package com.deskflow.allocation.application.port.out;

import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.util.List;
import java.util.Optional;

public interface ResourceCatalogPort {
    Optional<ResourceAllocationProfile> findResource(ResourceId resourceId);

    List<ResourceAllocationProfile> findActiveResources();
}
