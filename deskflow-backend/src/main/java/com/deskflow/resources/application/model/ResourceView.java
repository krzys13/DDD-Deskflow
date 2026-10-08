package com.deskflow.resources.application.model;

import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.resources.application.domain.model.vo.Location;

import java.util.UUID;

public record ResourceView(
        UUID id,
        String type,
        String name,
        String building,
        int floor,
        String number,
        boolean active,
        boolean available
) {
    public static ResourceView from(ResourceId id, ResourceType type, String name, Location location, boolean active, boolean available) {
        return new ResourceView(
                id.getValue(),
                type.name(),
                name,
                location.getBuilding(),
                location.getFloor(),
                location.getNumber(),
                active,
                available
        );
    }
}