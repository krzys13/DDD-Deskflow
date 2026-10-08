package com.deskflow.resources.application.domain.service;

import com.deskflow.resources.application.domain.model.OfficeResource;

public class MarkOfficeResourceUnavailableDomainService {

    public void markOfficeResourceUnavailable(OfficeResource resource) {
        resource.markAsUnavailable();
    }
}
