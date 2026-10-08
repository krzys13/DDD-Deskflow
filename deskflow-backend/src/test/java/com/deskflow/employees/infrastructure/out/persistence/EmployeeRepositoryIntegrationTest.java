package com.deskflow.employees.infrastructure.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class EmployeeRepositoryIntegrationTest {

    @Autowired private SpringDataEmployeeRepository jpa;

    @Test
    void save_and_find_employee() {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail("test@company.test");
        entity.setFirstName("Jan");
        entity.setLastName("Kowalski");
        entity.setRole("EMPLOYEE");
        entity.setPasswordHash("hash");
        jpa.save(entity);

        var found = jpa.findById(entity.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@company.test");
        assertThat(found.get().getFirstName()).isEqualTo("Jan");
        assertThat(found.get().getLastName()).isEqualTo("Kowalski");
        assertThat(found.get().getRole()).isEqualTo("EMPLOYEE");
    }

    @Test
    void find_by_email() {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail("unique@company.test");
        entity.setFirstName("A");
        entity.setLastName("B");
        entity.setRole("MANAGER");
        entity.setPasswordHash("hash");
        jpa.save(entity);

        var found = jpa.findByEmail("unique@company.test");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(entity.getId());
    }

    @Test
    void find_by_email_returns_empty_when_missing() {
        var found = jpa.findByEmail("nonexistent@company.test");

        assertThat(found).isEmpty();
    }

    @Test
    void find_returns_empty_when_missing() {
        var found = jpa.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    void saves_and_loads_allocation_limits() {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail("limits@company.test");
        entity.setFirstName("A");
        entity.setLastName("B");
        entity.setRole("EMPLOYEE");
        entity.setPasswordHash("hash");

        AllocationLimitEntity limit = new AllocationLimitEntity();
        limit.setId(UUID.randomUUID());
        limit.setResourceType("DESK");
        limit.setLimitAmount(3);
        entity.addAllocationLimit(limit);

        jpa.save(entity);

        var found = jpa.findById(entity.getId()).orElseThrow();
        assertThat(found.getAllocationLimits()).hasSize(1);
        assertThat(found.getAllocationLimits().getFirst().getResourceType()).isEqualTo("DESK");
        assertThat(found.getAllocationLimits().getFirst().getLimitAmount()).isEqualTo(3);
    }

    @Test
    void find_all_returns_all_employees() {
        EmployeeEntity e1 = new EmployeeEntity();
        e1.setId(UUID.randomUUID());
        e1.setEmail("e1@company.test");
        e1.setFirstName("E1");
        e1.setLastName("N1");
        e1.setRole("EMPLOYEE");
        e1.setPasswordHash("hash");

        EmployeeEntity e2 = new EmployeeEntity();
        e2.setId(UUID.randomUUID());
        e2.setEmail("e2@company.test");
        e2.setFirstName("E2");
        e2.setLastName("N2");
        e2.setRole("MANAGER");
        e2.setPasswordHash("hash");

        jpa.save(e1);
        jpa.save(e2);

        var all = jpa.findAll();

        assertThat(all).hasSize(2);
    }
}
