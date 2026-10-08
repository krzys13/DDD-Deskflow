package com.deskflow.resources.infrastructure.out.persistence;

import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.springframework.stereotype.Component;

@Component
class OfficeResourceMapper {

    OfficeResourceEntity toEntity(OfficeResource resource) {
        OfficeResourceEntity e = new OfficeResourceEntity();
        updateEntity(resource, e);
        return e;
    }

    void updateEntity(OfficeResource resource, OfficeResourceEntity e) {
        e.setId(resource.getId().getValue());
        e.setType(resource.getType().name());
        e.setName(resource.getName());
        e.setBuilding(resource.getLocation().getBuilding());
        e.setFloor(resource.getLocation().getFloor());
        e.setNumber(resource.getLocation().getNumber());
        e.setActive(resource.isActive());
        e.setAvailable(resource.isAvailable());
    }

    OfficeResource toDomain(OfficeResourceEntity e) {
        return OfficeResource.reconstitute(
                ResourceId.of(e.getId()),
                ResourceType.valueOf(e.getType()),
                e.getName(),
                new Location(e.getBuilding(), e.getFloor(), e.getNumber()),
                e.isActive(),
                e.isAvailable()
        );
    }
}
