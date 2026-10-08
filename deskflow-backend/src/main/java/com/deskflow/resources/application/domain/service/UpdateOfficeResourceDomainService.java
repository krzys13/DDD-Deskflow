package com.deskflow.resources.application.domain.service;

import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.model.vo.Location;

public class UpdateOfficeResourceDomainService {

    public void updateOfficeResource(OfficeResource resource, String name, Location location) {
        resource.update(name, location);
    }
}
