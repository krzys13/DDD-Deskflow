package com.deskflow.employees.application.service;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.employees.application.port.out.EmployeeRepositoryPort;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.employees.application.domain.model.vo.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeAppServiceTest {

    @Mock private EmployeeRepositoryPort employeeRepository;

    private EmployeeAppService appService;

    @BeforeEach
    void setUp() {
        appService = new EmployeeAppService(employeeRepository);
    }

    @Test
    void upsert_limit_changes_employee_limit_and_saves() {
        EmployeeId employeeId = EmployeeId.of(UUID.randomUUID());
        Employee employee = new Employee(employeeId, "a@test.pl", "Anna", "Test", Role.EMPLOYEE, Map.of());
        when(employeeRepository.find(employeeId)).thenReturn(Optional.of(employee));

        appService.upsertLimit(employeeId, "DESK", 4);

        assertThat(employee.limitForType(ResourceType.DESK)).isEqualTo(4);
        verify(employeeRepository).save(employee);
    }

    @Test
    void upsert_limit_throws_when_employee_missing() {
        EmployeeId employeeId = EmployeeId.of(UUID.randomUUID());
        when(employeeRepository.find(employeeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.upsertLimit(employeeId, "DESK", 4))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Employee not found");
    }

    @Test
    void upsert_limit_rejects_invalid_resource_type() {
        EmployeeId employeeId = EmployeeId.of(UUID.randomUUID());
        Employee employee = new Employee(employeeId, "a@test.pl", "Anna", "Test", Role.EMPLOYEE, Map.of());
        when(employeeRepository.find(employeeId)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> appService.upsertLimit(employeeId, "UNKNOWN", 4))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
