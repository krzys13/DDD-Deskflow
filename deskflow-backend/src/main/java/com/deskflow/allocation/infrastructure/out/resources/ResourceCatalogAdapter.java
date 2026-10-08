package com.deskflow.allocation.infrastructure.out.resources;

import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;
import com.deskflow.allocation.application.port.out.ResourceCatalogPort;
import com.deskflow.resources.application.model.ResourceView;
import com.deskflow.resources.application.port.in.ResourceManagementInputPort;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ResourceCatalogAdapter implements ResourceCatalogPort {

    private final ResourceManagementInputPort resourceManagementInputPort;

    public ResourceCatalogAdapter(ResourceManagementInputPort resourceManagementInputPort) {
        this.resourceManagementInputPort = resourceManagementInputPort;
    }

    @Override
    public Optional<ResourceAllocationProfile> findResource(ResourceId resourceId) {
        return resourceManagementInputPort.getResource(resourceId).map(this::toProfile);
    }

    @Override
    public List<ResourceAllocationProfile> findActiveResources() {
        return resourceManagementInputPort.getActiveResources().stream()
                .map(this::toProfile)
                .toList();
    }

    private ResourceAllocationProfile toProfile(ResourceView resource) {
        return new ResourceAllocationProfile(
                ResourceId.of(resource.id()),
                resource.type(),
                resource.name(),
                resource.building(),
                resource.floor(),
                resource.number(),
                resource.active(),
                resource.available()
        );
    }
}
