package com.deskflow.allocation.application.model;

import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;

import java.util.UUID;

public record ResourceCatalogItem(
        UUID id,
        String type,
        String name,
        String building,
        int floor,
        String number,
        boolean available
) {
    public static ResourceCatalogItem from(ResourceAllocationProfile resource) {
        return new ResourceCatalogItem(
                resource.id().getValue(),
                resource.type(),
                resource.name(),
                resource.building(),
                resource.floor(),
                resource.number(),
                resource.available()
        );
    }
}
