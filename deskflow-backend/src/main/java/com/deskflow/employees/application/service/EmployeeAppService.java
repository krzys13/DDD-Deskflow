package com.deskflow.employees.application.service;

import com.deskflow.employees.application.model.EmployeeCredentials;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.employees.application.port.out.EmployeeRepositoryPort;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EmployeeAppService implements EmployeeManagementInputPort {

    private final EmployeeRepositoryPort employeeRepository;

    public EmployeeAppService(EmployeeRepositoryPort employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Optional<Employee> getEmployee(EmployeeId id) {
        return employeeRepository.find(id);
    }

    @Override
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public Optional<EmployeeCredentials> getEmployeeCredentialsByEmail(String email) {
        return employeeRepository.findCredentialsByEmail(email);
    }

    @Override
    public List<Employee> listEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    @Transactional
    public void upsertLimit(EmployeeId employeeId, String resourceType, int amount) {
        Employee employee = employeeRepository.find(employeeId)
                .orElseThrow(() -> new NoSuchElementException("Employee not found: " + employeeId));
        employee.changeLimit(ResourceType.valueOf(resourceType), amount);
        employeeRepository.save(employee);
    }
}
