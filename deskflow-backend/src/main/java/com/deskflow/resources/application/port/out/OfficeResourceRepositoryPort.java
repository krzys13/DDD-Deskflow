package com.deskflow.resources.application.port.out;

import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.util.List;
import java.util.Optional;

public interface OfficeResourceRepositoryPort {

    void save(OfficeResource resource);

    Optional<OfficeResource> find(ResourceId id);

    List<OfficeResource> findActive();
}
