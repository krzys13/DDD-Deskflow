package com.deskflow.resources.application.domain.model;

import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.resources.application.domain.model.events.ResourceMadeAvailable;
import com.deskflow.resources.application.domain.model.events.ResourceMadeUnavailable;
import com.deskflow.resources.application.domain.model.events.ResourceUpdated;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceAvailableDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceUnavailableDomainService;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.domain.service.UpdateOfficeResourceDomainService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceLifecycleDomainServicesTest {

    private final UpdateOfficeResourceDomainService updateService = new UpdateOfficeResourceDomainService();
    private final MarkOfficeResourceUnavailableDomainService unavailableService = new MarkOfficeResourceUnavailableDomainService();
    private final MarkOfficeResourceAvailableDomainService availableService = new MarkOfficeResourceAvailableDomainService();

    @Test
    void update_service_updates_name_and_location_and_records_event() {
        OfficeResource resource = resource();
        Location newLocation = new Location("B", 2, "202");

        updateService.updateOfficeResource(resource, "New desk", newLocation);

        assertThat(resource.getName()).isEqualTo("New desk");
        assertThat(resource.getLocation()).isEqualTo(newLocation);
        assertThat(resource.flushEvents()).hasAtLeastOneElementOfType(ResourceUpdated.class);
    }

    @Test
    void mark_unavailable_service_marks_resource_unavailable_and_records_event() {
        OfficeResource resource = resource();

        unavailableService.markOfficeResourceUnavailable(resource);

        assertThat(resource.isAvailable()).isFalse();
        assertThat(resource.flushEvents()).hasAtLeastOneElementOfType(ResourceMadeUnavailable.class);
    }

    @Test
    void mark_available_service_marks_resource_available_and_records_event() {
        OfficeResource resource = resource();
        resource.markAsUnavailable();
        resource.flushEvents();

        availableService.markOfficeResourceAvailable(resource);

        assertThat(resource.isAvailable()).isTrue();
        assertThat(resource.flushEvents()).hasAtLeastOneElementOfType(ResourceMadeAvailable.class);
    }

    private OfficeResource resource() {
        return new OfficeResourceFactory().create(ResourceType.DESK, "Desk", new Location("A", 1, "101"));
    }
}
