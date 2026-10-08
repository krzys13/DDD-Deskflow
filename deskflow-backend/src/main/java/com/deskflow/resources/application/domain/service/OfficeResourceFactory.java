package com.deskflow.resources.application.domain.service;

import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;

import java.util.UUID;

public class OfficeResourceFactory {

    public OfficeResource create(ResourceType type, String name, Location location) {
        return OfficeResource.create(
                ResourceId.of(UUID.randomUUID()),
                type,
                name,
                location
        );
    }
}
