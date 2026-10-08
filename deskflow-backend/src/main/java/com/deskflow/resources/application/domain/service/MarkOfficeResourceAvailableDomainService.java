package com.deskflow.resources.application.domain.service;

import com.deskflow.resources.application.domain.model.OfficeResource;

public class MarkOfficeResourceAvailableDomainService {

    public void markOfficeResourceAvailable(OfficeResource resource) {
        resource.markAsAvailable();
    }
}
