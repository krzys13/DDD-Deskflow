package com.deskflow.allocation.application.service;

import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.port.out.EmployeeProfilePort;
import com.deskflow.allocation.application.port.out.ResourceCatalogPort;
import com.deskflow.allocation.application.domain.model.EmployeeAllocationProfile;
import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.domain.service.CancelAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.EnsureEmployeeWithinLimitDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceAvailableInPeriodDomainService;
import com.deskflow.allocation.application.domain.service.EnsureResourceEligibleForAllocationDomainService;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationRequestAppServiceMissingDataTest {

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
    void submit_throws_when_employee_missing() {
        when(employeeProfilePort.findEmployee(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.submitRequest(employeeId, resourceId, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5), "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void submit_throws_when_resource_missing() {
        when(employeeProfilePort.findEmployee(employeeId)).thenReturn(Optional.of(employee));
        when(resourceCatalogPort.findResource(resourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.submitRequest(employeeId, resourceId, LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5), "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Resource not found");
    }

    @Test
    void cancel_throws_when_request_missing() {
        AllocationRequestId requestId = AllocationRequestId.newId();
        when(requestRepository.find(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.cancelRequest(requestId, employeeId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Allocation request not found");
    }
}
