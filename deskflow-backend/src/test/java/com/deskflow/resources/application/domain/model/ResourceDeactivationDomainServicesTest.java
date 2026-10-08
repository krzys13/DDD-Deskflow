package com.deskflow.resources.application.domain.model;

import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.resources.application.domain.model.vo.AllocationUsageStatus;
import com.deskflow.resources.application.domain.service.DeactivateOfficeResourceDomainService;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceDeactivationDomainServicesTest {

    private final DeactivateOfficeResourceDomainService deactivateOfficeResourceDomainService =
            new DeactivateOfficeResourceDomainService();

    @ParameterizedTest
    @EnumSource(value = AllocationUsageStatus.class, names = {"IN_USE", "APPROVED", "REQUESTED", "OVERDUE"})
    void deactivate_throws_when_resource_has_blocking_allocation_status(AllocationUsageStatus status) {
        OfficeResource resource = new OfficeResourceFactory().create(ResourceType.DESK, "Desk", new Location("A", 1, "101"));

        assertThatThrownBy(() -> deactivateOfficeResourceDomainService.deactivateOfficeResource(resource, List.of(status)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("active allocation requests");
    }

    @Test
    void deactivate_resource_when_no_related_allocations() {
        OfficeResource resource = new OfficeResourceFactory().create(ResourceType.DESK, "Desk", new Location("A", 1, "101"));

        deactivateOfficeResourceDomainService.deactivateOfficeResource(resource, List.of());

        assertThat(resource.isActive()).isFalse();
        assertThat(resource.isAvailable()).isFalse();
    }
}
