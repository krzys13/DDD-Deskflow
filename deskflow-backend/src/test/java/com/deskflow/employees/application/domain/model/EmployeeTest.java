package com.deskflow.employees.application.domain.model;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.employees.application.domain.model.vo.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeTest {

    @Test
    void limit_for_missing_resource_type_defaults_to_one() {
        Employee employee = new Employee(EmployeeId.of(UUID.randomUUID()), "a@test.pl", "Anna", "Test",
                Role.EMPLOYEE, Map.of(ResourceType.DESK, 3));

        assertThat(employee.limitForType(ResourceType.LAPTOP)).isEqualTo(1);
    }

    @Test
    void change_limit_updates_limit_for_resource_type() {
        Employee employee = new Employee(EmployeeId.of(UUID.randomUUID()), "a@test.pl", "Anna", "Test",
                Role.EMPLOYEE, Map.of());

        employee.changeLimit(ResourceType.DESK, 0);

        assertThat(employee.limitForType(ResourceType.DESK)).isZero();
    }

    @Test
    void change_limit_rejects_negative_amount() {
        Employee employee = new Employee(EmployeeId.of(UUID.randomUUID()), "a@test.pl", "Anna", "Test",
                Role.EMPLOYEE, Map.of());

        assertThatThrownBy(() -> employee.changeLimit(ResourceType.DESK, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    void getters_return_expected_values() {
        var id = EmployeeId.of(UUID.randomUUID());
        var employee = new Employee(id, "test@test.pl", "Jan", "Kowalski", Role.MANAGER,
                Map.of(ResourceType.DESK, 2));

        assertThat(employee.getId()).isEqualTo(id);
        assertThat(employee.getEmail()).isEqualTo("test@test.pl");
        assertThat(employee.getFirstName()).isEqualTo("Jan");
        assertThat(employee.getLastName()).isEqualTo("Kowalski");
        assertThat(employee.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    void change_limit_overwrites_existing_limit() {
        Employee employee = new Employee(EmployeeId.of(UUID.randomUUID()), "a@test.pl", "Anna", "Test",
                Role.EMPLOYEE, Map.of(ResourceType.DESK, 3));

        employee.changeLimit(ResourceType.DESK, 5);

        assertThat(employee.limitForType(ResourceType.DESK)).isEqualTo(5);
    }

    @Test
    void constructor_makes_defensive_copy_of_limits() {
        var mutableLimits = new java.util.HashMap<ResourceType, Integer>();
        mutableLimits.put(ResourceType.DESK, 3);
        Employee employee = new Employee(EmployeeId.of(UUID.randomUUID()), "a@test.pl", "Anna", "Test",
                Role.EMPLOYEE, mutableLimits);

        mutableLimits.put(ResourceType.LAPTOP, 2);

        assertThat(employee.limitForType(ResourceType.LAPTOP)).isEqualTo(1);
    }

    @Test
    void getLimits_returns_unmodifiable_map() {
        Employee employee = new Employee(EmployeeId.of(UUID.randomUUID()), "a@test.pl", "Anna", "Test",
                Role.EMPLOYEE, Map.of(ResourceType.DESK, 3));

        assertThatThrownBy(() -> employee.getLimits().put(ResourceType.LAPTOP, 2))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void limit_for_zero_is_valid() {
        Employee employee = new Employee(EmployeeId.of(UUID.randomUUID()), "a@test.pl", "Anna", "Test",
                Role.EMPLOYEE, Map.of(ResourceType.DESK, 0));

        assertThat(employee.limitForType(ResourceType.DESK)).isZero();
    }
}
