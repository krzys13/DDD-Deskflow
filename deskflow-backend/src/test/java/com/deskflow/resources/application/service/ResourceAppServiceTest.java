package com.deskflow.resources.application.service;

import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.resources.application.port.out.OfficeResourceRepositoryPort;
import com.deskflow.resources.application.port.out.ResourceAllocationRequestPort;
import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.model.events.ResourceDeactivated;
import com.deskflow.resources.application.domain.model.events.ResourceMadeUnavailable;
import com.deskflow.resources.application.domain.model.events.ResourceUpdated;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.resources.application.domain.service.DeactivateOfficeResourceDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceAvailableDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceUnavailableDomainService;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.domain.service.UpdateOfficeResourceDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceAppServiceTest {

    @Mock private OfficeResourceRepositoryPort resourceRepository;
    @Mock private ResourceAllocationRequestPort allocationRequestPort;
    @Mock private DomainEventPublisherPort eventPublisher;

    private ResourceAppService appService;

    @BeforeEach
    void setUp() {
        appService = new ResourceAppService(resourceRepository, allocationRequestPort, new OfficeResourceFactory(),
                new UpdateOfficeResourceDomainService(), new MarkOfficeResourceUnavailableDomainService(),
                new MarkOfficeResourceAvailableDomainService(),
                new DeactivateOfficeResourceDomainService(),
                eventPublisher);
    }

    @Test
    void add_resource_creates_active_available_resource_and_saves() {
        ArgumentCaptor<OfficeResource> resourceCaptor = ArgumentCaptor.forClass(OfficeResource.class);

        ResourceId id = appService.addResource(ResourceType.DESK, "Desk 1", "A", 1, "101");

        verify(resourceRepository).save(resourceCaptor.capture());
        OfficeResource saved = resourceCaptor.getValue();
        assertThat(id).isEqualTo(saved.getId());
        assertThat(saved.getType()).isEqualTo(ResourceType.DESK);
        assertThat(saved.getName()).isEqualTo("Desk 1");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isAvailable()).isTrue();
    }

    @Test
    void update_resource_saves_and_publishes_update_event() {
        OfficeResource resource = resource();
        when(resourceRepository.find(resource.getId())).thenReturn(Optional.of(resource));

        appService.updateResource(resource.getId(), "New name", "B", 2, "202");

        verify(resourceRepository).save(resource);
        verify(eventPublisher).publish(any(ResourceUpdated.class));
        assertThat(resource.getName()).isEqualTo("New name");
        assertThat(resource.getLocation()).isEqualTo(new Location("B", 2, "202"));
    }

    @Test
    void mark_unavailable_saves_and_publishes_event() {
        OfficeResource resource = resource();
        when(resourceRepository.find(resource.getId())).thenReturn(Optional.of(resource));

        appService.markUnavailable(resource.getId());

        verify(resourceRepository).save(resource);
        verify(eventPublisher).publish(any(ResourceMadeUnavailable.class));
        assertThat(resource.isAvailable()).isFalse();
    }

    @Test
    void deactivate_resource_checks_related_allocations_saves_and_publishes_event() {
        OfficeResource resource = resource();
        when(resourceRepository.find(resource.getId())).thenReturn(Optional.of(resource));
        when(allocationRequestPort.findStatusesForResource(resource.getId())).thenReturn(List.of());

        appService.deactivateResource(resource.getId());

        verify(allocationRequestPort).findStatusesForResource(resource.getId());
        verify(resourceRepository).save(resource);
        verify(eventPublisher).publish(any(ResourceDeactivated.class));
        assertThat(resource.isActive()).isFalse();
    }

    @Test
    void mark_unavailable_throws_when_resource_missing() {
        ResourceId resourceId = ResourceId.of(UUID.randomUUID());
        when(resourceRepository.find(resourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.markUnavailable(resourceId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Resource not found");
    }

    private OfficeResource resource() {
        return new OfficeResourceFactory().create(ResourceType.DESK, "Desk", new Location("A", 1, "101"));
    }
}
