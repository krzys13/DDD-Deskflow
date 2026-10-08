package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceAvailabilityDomainServicesTest {

    private final EnsureResourceAvailableInPeriodDomainService ensureResourceAvailableInPeriodDomainService = new EnsureResourceAvailableInPeriodDomainService();
    private final EnsureResourceEligibleForAllocationDomainService ensureResourceEligibleForAllocationDomainService = new EnsureResourceEligibleForAllocationDomainService();

    @Test
    void requires_available_when_no_overlapping_requests() {
        assertThatCode(() -> ensureResourceAvailableInPeriodDomainService.ensureResourceAvailableInPeriod(Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    void throws_when_overlapping_requests_exist() {
        AllocationRequest existing = AllocationRequest.reconstitute(
                AllocationRequestId.newId(), EmployeeId.of(UUID.randomUUID()), ResourceId.of(UUID.randomUUID()),
                new DateRange(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10)),
                "test", LocalDateTime.now(), RequestStatus.ACTIVE, null, null, null
        );

        assertThatThrownBy(() -> ensureResourceAvailableInPeriodDomainService.ensureResourceAvailableInPeriod(List.of(existing)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available for the requested period");
    }
    
    @Test
    void requires_eligible_for_allocation_does_not_throw_when_active_and_available() {
        ResourceAllocationProfile resource = new ResourceAllocationProfile(
                ResourceId.of(UUID.randomUUID()), ResourceType.DESK.name(), "D1", "A", 1, "1", true, true);
        assertThatCode(() -> ensureResourceEligibleForAllocationDomainService.ensureResourceEligibleForAllocation(resource))
                .doesNotThrowAnyException();
    }
    
    @Test
    void requires_eligible_throws_when_unavailable() {
        ResourceAllocationProfile resource = new ResourceAllocationProfile(
                ResourceId.of(UUID.randomUUID()), ResourceType.DESK.name(), "D1", "A", 1, "1", true, false);
        assertThatThrownBy(() -> ensureResourceEligibleForAllocationDomainService.ensureResourceEligibleForAllocation(resource))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Resource is not available for allocation");
    }
}
