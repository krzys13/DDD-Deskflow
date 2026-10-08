package com.deskflow.employees.infrastructure.out.persistence;

import com.deskflow.employees.application.model.EmployeeCredentials;
import com.deskflow.employees.application.port.out.EmployeeRepositoryPort;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class HibernateEmployeeRepositoryAdapter implements EmployeeRepositoryPort {

    private final SpringDataEmployeeRepository jpa;
    private final SpringDataAllocationLimitRepository limitJpa;
    private final EmployeeMapper mapper;

    public HibernateEmployeeRepositoryAdapter(SpringDataEmployeeRepository jpa,
                                              SpringDataAllocationLimitRepository limitJpa,
                                              EmployeeMapper mapper) {
        this.jpa = jpa;
        this.limitJpa = limitJpa;
        this.mapper = mapper;
    }

    @Override
    public Optional<Employee> find(EmployeeId id) {
        return jpa.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<EmployeeCredentials> findCredentialsByEmail(String email) {
        return jpa.findByEmail(email)
                .map(employee -> new EmployeeCredentials(employee.getEmail(), employee.getPasswordHash(), employee.getRole()));
    }

    @Override
    public void save(Employee employee) {
        EmployeeEntity entity = jpa.findById(employee.getId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employee.getId()));
        employee.getLimits().forEach((type, amount) -> upsertLimit(entity, type.name(), amount));
        jpa.save(entity);
    }

    @Override
    public List<Employee> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    private void upsertLimit(EmployeeEntity employee, String resourceType, int amount) {
        AllocationLimitEntity limit = limitJpa
                .findByEmployeeIdAndResourceType(employee.getId(), resourceType)
                .orElseGet(() -> {
                    AllocationLimitEntity l = new AllocationLimitEntity();
                    l.setId(UUID.randomUUID());
                    l.setEmployee(employee);
                    l.setResourceType(resourceType);
                    return l;
                });
        limit.setLimitAmount(amount);
        limitJpa.save(limit);
    }
}
