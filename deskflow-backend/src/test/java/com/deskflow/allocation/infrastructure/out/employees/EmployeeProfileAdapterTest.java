package com.deskflow.allocation.infrastructure.out.employees;

import com.deskflow.allocation.application.domain.model.EmployeeAllocationProfile;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.employees.application.domain.model.vo.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeProfileAdapterTest {

    @Mock private EmployeeManagementInputPort employeeManagementInputPort;

    private EmployeeProfileAdapter adapter;
    private EmployeeId employeeId;

    @BeforeEach
    void setUp() {
        adapter = new EmployeeProfileAdapter(employeeManagementInputPort);
        employeeId = EmployeeId.of(UUID.randomUUID());
    }

    @Test
    void findEmployee_returns_profile_with_limits() {
        var employee = new Employee(employeeId, "test@company.test", "Jan", "Kowalski", Role.EMPLOYEE,
                Map.of(ResourceType.DESK, 3, ResourceType.LAPTOP, 1));
        when(employeeManagementInputPort.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        var result = adapter.findEmployee(employeeId);

        assertThat(result).isPresent();
        assertThat(result.get().limitsByResourceType()).containsEntry("DESK", 3);
        assertThat(result.get().limitsByResourceType()).containsEntry("LAPTOP", 1);
    }

    @Test
    void findEmployee_returns_empty_when_not_found() {
        when(employeeManagementInputPort.getEmployee(employeeId)).thenReturn(Optional.empty());

        var result = adapter.findEmployee(employeeId);

        assertThat(result).isEmpty();
    }

    @Test
    void findEmployee_empty_limits_when_employee_has_none() {
        var employee = new Employee(employeeId, "test@company.test", "Jan", "Kowalski", Role.EMPLOYEE, Map.of());
        when(employeeManagementInputPort.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        var result = adapter.findEmployee(employeeId);

        assertThat(result).isPresent();
        assertThat(result.get().limitsByResourceType()).isEmpty();
    }
}
