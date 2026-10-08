package com.deskflow.resources.application.service;

import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.resources.application.model.ResourceView;
import com.deskflow.resources.application.port.in.ResourceManagementInputPort;
import com.deskflow.resources.application.port.out.OfficeResourceRepositoryPort;
import com.deskflow.resources.application.port.out.ResourceAllocationRequestPort;
import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.domain.service.DeactivateOfficeResourceDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceAvailableDomainService;
import com.deskflow.resources.application.domain.service.MarkOfficeResourceUnavailableDomainService;
import com.deskflow.resources.application.domain.service.UpdateOfficeResourceDomainService;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ResourceAppService implements ResourceManagementInputPort {

    private final OfficeResourceRepositoryPort resourceRepository;
    private final ResourceAllocationRequestPort allocationRequestPort;
    private final OfficeResourceFactory factory;
    private final UpdateOfficeResourceDomainService updateOfficeResourceDomainService;
    private final MarkOfficeResourceUnavailableDomainService markOfficeResourceUnavailableDomainService;
    private final MarkOfficeResourceAvailableDomainService markOfficeResourceAvailableDomainService;
    private final DeactivateOfficeResourceDomainService deactivateOfficeResourceDomainService;
    private final DomainEventPublisherPort eventPublisher;

    public ResourceAppService(OfficeResourceRepositoryPort resourceRepository,
                            ResourceAllocationRequestPort allocationRequestPort,
                            OfficeResourceFactory factory,
                            UpdateOfficeResourceDomainService updateOfficeResourceDomainService,
                            MarkOfficeResourceUnavailableDomainService markOfficeResourceUnavailableDomainService,
                            MarkOfficeResourceAvailableDomainService markOfficeResourceAvailableDomainService,
                            DeactivateOfficeResourceDomainService deactivateOfficeResourceDomainService,
                            DomainEventPublisherPort eventPublisher) {
        this.resourceRepository = resourceRepository;
        this.allocationRequestPort = allocationRequestPort;
        this.factory = factory;
        this.updateOfficeResourceDomainService = updateOfficeResourceDomainService;
        this.markOfficeResourceUnavailableDomainService = markOfficeResourceUnavailableDomainService;
        this.markOfficeResourceAvailableDomainService = markOfficeResourceAvailableDomainService;
        this.deactivateOfficeResourceDomainService = deactivateOfficeResourceDomainService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ResourceId addResource(ResourceType type, String name, String building, int floor, String number) {
        Location location = new Location(building, floor, number);
        OfficeResource resource = factory.create(type, name, location);
        resourceRepository.save(resource);
        return resource.getId();
    }

    @Override
    public void updateResource(ResourceId resourceId, String name, String building, int floor, String number) {
        OfficeResource resource = findResource(resourceId);
        Location location = new Location(building, floor, number);
        updateOfficeResourceDomainService.updateOfficeResource(resource, name, location);
        resourceRepository.save(resource);
        publishAndClear(resource);
    }

    @Override
    public void markUnavailable(ResourceId resourceId) {
        OfficeResource resource = findResource(resourceId);
        markOfficeResourceUnavailableDomainService.markOfficeResourceUnavailable(resource);
        resourceRepository.save(resource);
        publishAndClear(resource);
    }

    @Override
    public void markAvailable(ResourceId resourceId) {
        OfficeResource resource = findResource(resourceId);
        markOfficeResourceAvailableDomainService.markOfficeResourceAvailable(resource);
        resourceRepository.save(resource);
        publishAndClear(resource);
    }

    @Override
    public void deactivateResource(ResourceId resourceId) {
        OfficeResource resource = findResource(resourceId);
        deactivateOfficeResourceDomainService.deactivateOfficeResource(resource, allocationRequestPort.findStatusesForResource(resourceId));
        resourceRepository.save(resource);
        publishAndClear(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResourceView> getResource(ResourceId resourceId) {
        return resourceRepository.find(resourceId).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceView> getActiveResources() {
        return resourceRepository.findActive().stream().map(this::toView).toList();
    }

    private ResourceView toView(OfficeResource r) {
        return ResourceView.from(r.getId(), r.getType(), r.getName(), r.getLocation(), r.isActive(), r.isAvailable());
    }

    private OfficeResource findResource(ResourceId id) {
        return resourceRepository.find(id)
                .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
    }

    private void publishAndClear(OfficeResource resource) {
        for (DomainEvent event : resource.flushEvents()) {
            eventPublisher.publish(event);
        }
    }
}
