package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.EmployeeAllocationProfile;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeLimitDomainServiceTest {

    private final EnsureEmployeeWithinLimitDomainService service = new EnsureEmployeeWithinLimitDomainService();

    @Test
    void allows_allocation_when_current_usage_is_below_limit() {
        EmployeeAllocationProfile employee = new EmployeeAllocationProfile(Map.of(ResourceType.DESK.name(), 2));

        assertThatCode(() -> service.ensureEmployeeWithinLimit(employee, ResourceType.DESK.name(), 1))
                .doesNotThrowAnyException();
    }

    @Test
    void rejects_allocation_when_current_usage_equals_limit() {
        EmployeeAllocationProfile employee = new EmployeeAllocationProfile(Map.of(ResourceType.DESK.name(), 2));

        assertThatThrownBy(() -> service.ensureEmployeeWithinLimit(employee, ResourceType.DESK.name(), 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allocation limit");
    }

    @Test
    void rejects_allocation_when_current_usage_exceeds_limit() {
        EmployeeAllocationProfile employee = new EmployeeAllocationProfile(Map.of(ResourceType.DESK.name(), 2));

        assertThatThrownBy(() -> service.ensureEmployeeWithinLimit(employee, ResourceType.DESK.name(), 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allocation limit");
    }
}
