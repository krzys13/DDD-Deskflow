package com.deskflow.allocation.application.service;

import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.port.out.EmployeeProfilePort;
import com.deskflow.allocation.application.port.out.ResourceCatalogPort;
import com.deskflow.allocation.application.domain.model.EmployeeAllocationProfile;
import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.domain.service.CancelAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.EnsureEmployeeWithinLimitDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceAvailableInPeriodDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceEligibleForAllocationDomainService;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationRequestAppServiceTest {

    @Mock private AllocationRequestRepositoryPort requestRepository;
    @Mock private ResourceCatalogPort resourceCatalogPort;
    @Mock private EmployeeProfilePort employeeProfilePort;
    @Mock private DomainEventPublisherPort eventPublisher;

    private AllocationRequestAppService appService;
    private EmployeeId employeeId;
    private ResourceId resourceId;
    private ResourceAllocationProfile resource;
    private EmployeeAllocationProfile employee;

    @BeforeEach
    void setUp() {
        employeeId = EmployeeId.of(UUID.randomUUID());
        resourceId = ResourceId.of(UUID.randomUUID());

        resource = new ResourceAllocationProfile(resourceId, ResourceType.DESK.name(), "Test desk", "A", 1, "1", true, true);
        employee = new EmployeeAllocationProfile(Map.of(ResourceType.DESK.name(), 3));

        appService = new AllocationRequestAppService(
                requestRepository, resourceCatalogPort, employeeProfilePort,
                new AllocationRequestFactory(), new CancelAllocationRequestDomainService(),
                new EnsureResourceEligibleForAllocationDomainService(), new EnsureResourceAvailableInPeriodDomainService(),
                new EnsureEmployeeWithinLimitDomainService(), eventPublisher
        );
    }

    @Test
    void submit_saves_request_when_within_limit_and_resource_available() {
        when(employeeProfilePort.findEmployee(employeeId)).thenReturn(Optional.of(employee));
        when(resourceCatalogPort.findResource(resourceId)).thenReturn(Optional.of(resource));
        when(requestRepository.findActiveForEmployee(employeeId)).thenReturn(Collections.emptyList());
        when(requestRepository.findActiveForResource(eq(resourceId), any(DateRange.class))).thenReturn(Collections.emptyList());

        AllocationRequestId id = appService.submitRequest(employeeId, resourceId, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5), "test");

        assertThat(id).isNotNull();
        verify(requestRepository).save(any(AllocationRequest.class));
    }

    @Test
    void submit_throws_when_limit_exceeded() {
        AllocationRequest a1 = AllocationRequest.reconstitute(
                AllocationRequestId.newId(), employeeId, resourceId,
                new DateRange(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 10)),
                "x", LocalDateTime.now(), RequestStatus.ACTIVE, LocalDateTime.now(), null, null
        );
        AllocationRequest a2 = AllocationRequest.reconstitute(
                AllocationRequestId.newId(), employeeId, resourceId,
                new DateRange(LocalDate.of(2025, 6, 11), LocalDate.of(2025, 6, 20)),
                "x", LocalDateTime.now(), RequestStatus.ACTIVE, LocalDateTime.now(), null, null
        );
        AllocationRequest a3 = AllocationRequest.reconstitute(
                AllocationRequestId.newId(), employeeId, resourceId,
                new DateRange(LocalDate.of(2025, 6, 21), LocalDate.of(2025, 6, 30)),
                "x", LocalDateTime.now(), RequestStatus.ACTIVE, LocalDateTime.now(), null, null
        );
        when(employeeProfilePort.findEmployee(employeeId)).thenReturn(Optional.of(employee));
        when(resourceCatalogPort.findResource(resourceId)).thenReturn(Optional.of(resource));
        when(requestRepository.findActiveForEmployee(employeeId))
                .thenReturn(List.of(a1, a2, a3));

        assertThatThrownBy(() -> appService.submitRequest(employeeId, resourceId, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5), "test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void submit_throws_when_resource_unavailable() {
        when(employeeProfilePort.findEmployee(employeeId)).thenReturn(Optional.of(employee));
        when(resourceCatalogPort.findResource(resourceId)).thenReturn(Optional.of(resource));
        when(requestRepository.findActiveForEmployee(employeeId)).thenReturn(Collections.emptyList());

        AllocationRequest blocking = AllocationRequest.reconstitute(
                AllocationRequestId.newId(), EmployeeId.of(UUID.randomUUID()), resourceId,
                new DateRange(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10)),
                "x", LocalDateTime.now(), RequestStatus.ACTIVE, LocalDateTime.now(), null, null
        );
        when(requestRepository.findActiveForResource(eq(resourceId), any(DateRange.class)))
                .thenReturn(List.of(blocking));

        assertThatThrownBy(() -> appService.submitRequest(employeeId, resourceId, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5), "test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("available");
    }

    @Test
    void cancel_changes_status_and_saves() {
        AllocationRequestId requestId = AllocationRequestId.newId();
        AllocationRequest request = AllocationRequest.reconstitute(
                requestId, employeeId, resourceId,
                new DateRange(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5)),
                "test", LocalDateTime.now(),
                RequestStatus.SUBMITTED, null, null, null
        );
        when(requestRepository.find(requestId)).thenReturn(Optional.of(request));

        appService.cancelRequest(requestId, employeeId);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        verify(requestRepository).save(request);
    }
}
