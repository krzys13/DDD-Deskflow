package com.deskflow.allocation.application.service;

import com.deskflow.allocation.application.port.in.AllocationRequestInputPort;
import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.application.model.ResourceCatalogItem;
import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.port.out.EmployeeProfilePort;
import com.deskflow.allocation.application.port.out.ResourceCatalogPort;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;
import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.domain.service.CancelAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.EnsureEmployeeWithinLimitDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceAvailableInPeriodDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceEligibleForAllocationDomainService;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class AllocationRequestAppService implements AllocationRequestInputPort {

    private final AllocationRequestRepositoryPort requestRepository;
    private final ResourceCatalogPort resourceCatalogPort;
    private final EmployeeProfilePort employeeProfilePort;
    private final AllocationRequestFactory factory;
    private final CancelAllocationRequestDomainService cancelAllocationRequestDomainService;
    private final EnsureResourceEligibleForAllocationDomainService ensureResourceEligibleForAllocationDomainService;
    private final EnsureResourceAvailableInPeriodDomainService ensureResourceAvailableInPeriodDomainService;
    private final EnsureEmployeeWithinLimitDomainService ensureEmployeeWithinLimitDomainService;
    private final DomainEventPublisherPort eventPublisher;

    public AllocationRequestAppService(AllocationRequestRepositoryPort requestRepository,
                                      ResourceCatalogPort resourceCatalogPort,
                                      EmployeeProfilePort employeeProfilePort,
                                      AllocationRequestFactory factory,
                                      CancelAllocationRequestDomainService cancelAllocationRequestDomainService,
                                      EnsureResourceEligibleForAllocationDomainService ensureResourceEligibleForAllocationDomainService,
                                      EnsureResourceAvailableInPeriodDomainService ensureResourceAvailableInPeriodDomainService,
                                      EnsureEmployeeWithinLimitDomainService ensureEmployeeWithinLimitDomainService,
                                      DomainEventPublisherPort eventPublisher) {
        this.requestRepository = requestRepository;
        this.resourceCatalogPort = resourceCatalogPort;
        this.employeeProfilePort = employeeProfilePort;
        this.factory = factory;
        this.cancelAllocationRequestDomainService = cancelAllocationRequestDomainService;
        this.ensureResourceEligibleForAllocationDomainService = ensureResourceEligibleForAllocationDomainService;
        this.ensureResourceAvailableInPeriodDomainService = ensureResourceAvailableInPeriodDomainService;
        this.ensureEmployeeWithinLimitDomainService = ensureEmployeeWithinLimitDomainService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AllocationRequestId submitRequest(EmployeeId employeeId, ResourceId resourceId,
                                              LocalDate dateFrom, LocalDate dateTo, String justification) {
        var employee = employeeProfilePort.findEmployee(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        ResourceAllocationProfile requestedResource = resourceCatalogPort.findResource(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + resourceId));

        ensureResourceEligibleForAllocationDomainService.ensureResourceEligibleForAllocation(requestedResource);

        String requestedType = requestedResource.type();
        long currentActiveForType = requestRepository.findActiveForEmployee(employeeId).stream()
                .map(AllocationRequest::getResourceId)
                .map(resourceCatalogPort::findResource)
                .flatMap(java.util.Optional::stream)
                .filter(resource -> resource.type().equals(requestedType))
                .count();

        ensureEmployeeWithinLimitDomainService.ensureEmployeeWithinLimit(employee, requestedType, currentActiveForType);

        DateRange dateRange = new DateRange(dateFrom, dateTo);

        List<AllocationRequest> overlappingRequests = requestRepository.findActiveForResource(resourceId, dateRange);
        ensureResourceAvailableInPeriodDomainService.ensureResourceAvailableInPeriod(overlappingRequests);

        AllocationRequest request = factory.create(employeeId, resourceId, dateRange, justification);
        requestRepository.save(request);
        publishAndClear(request);
        return request.getId();
    }

    @Override
    public void cancelRequest(AllocationRequestId requestId, EmployeeId employeeId) {
        AllocationRequest request = findRequest(requestId);
        cancelAllocationRequestDomainService.cancelAllocationRequest(request, employeeId);
        requestRepository.save(request);
        publishAndClear(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceCatalogItem> browseResources() {
        return resourceCatalogPort.findActiveResources().stream()
                .map(ResourceCatalogItem::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationRequestView> browseHistory(EmployeeId employeeId) {
        return requestRepository.findForEmployee(employeeId).stream()
                .map(AllocationRequestView::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationRequestView> findActiveForResource(ResourceId resourceId) {
        return requestRepository.findActiveForResource(resourceId).stream()
                .map(AllocationRequestView::from)
                .toList();
    }

    private AllocationRequest findRequest(AllocationRequestId id) {
        return requestRepository.find(id)
                .orElseThrow(() -> new NoSuchElementException("Allocation request not found: " + id));
    }

    private void publishAndClear(AllocationRequest request) {
        for (DomainEvent event : request.flushEvents()) {
            eventPublisher.publish(event);
        }
    }
}
