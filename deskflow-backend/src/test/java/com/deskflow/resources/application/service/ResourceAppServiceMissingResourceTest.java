package com.deskflow.resources.application.service;

import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import com.deskflow.resources.application.port.out.OfficeResourceRepositoryPort;
import com.deskflow.resources.application.port.out.ResourceAllocationRequestPort;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.domain.service.DeactivateOfficeResourceDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceAvailableDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceUnavailableDomainService;
import com.deskflow.resources.application.domain.service.UpdateOfficeResourceDomainService;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceAppServiceMissingResourceTest {

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
    void update_throws_when_resource_missing() {
        ResourceId resourceId = ResourceId.of(java.util.UUID.randomUUID());
        when(resourceRepository.find(resourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.updateResource(resourceId, "Desk", "A", 1, "101"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Resource not found");
    }
}
