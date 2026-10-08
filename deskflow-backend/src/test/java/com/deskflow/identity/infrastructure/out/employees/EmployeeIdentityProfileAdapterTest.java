package com.deskflow.identity.infrastructure.out.employees;

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
class EmployeeIdentityProfileAdapterTest {

    @Mock private EmployeeManagementInputPort employeeManagementInputPort;

    private EmployeeIdentityProfileAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EmployeeIdentityProfileAdapter(employeeManagementInputPort);
    }

    @Test
    void findByEmail_returns_mapped_profile_when_found() {
        var employeeId = EmployeeId.of(UUID.randomUUID());
        var employee = new Employee(employeeId, "test@company.test", "Jan", "Kowalski", Role.MANAGER, Map.of());
        when(employeeManagementInputPort.getEmployeeByEmail("test@company.test"))
                .thenReturn(Optional.of(employee));

        var result = adapter.findByEmail("test@company.test");

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("test@company.test");
        assertThat(result.get().role()).isEqualTo("MANAGER");
        assertThat(result.get().firstName()).isEqualTo("Jan");
        assertThat(result.get().lastName()).isEqualTo("Kowalski");
        assertThat(result.get().employeeId()).isEqualTo(employeeId.toString());
    }

    @Test
    void findByEmail_returns_empty_when_not_found() {
        when(employeeManagementInputPort.getEmployeeByEmail("missing@company.test"))
                .thenReturn(Optional.empty());

        var result = adapter.findByEmail("missing@company.test");

        assertThat(result).isEmpty();
    }
}
