package com.deskflow.employees.application.port.in;

import com.deskflow.employees.application.model.EmployeeCredentials;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.common.application.domain.model.vo.EmployeeId;

import java.util.Optional;

public interface EmployeeManagementInputPort {
    Optional<Employee> getEmployee(EmployeeId id);
    Optional<Employee> getEmployeeByEmail(String email);
    Optional<EmployeeCredentials> getEmployeeCredentialsByEmail(String email);
    java.util.List<Employee> listEmployees();
    void upsertLimit(EmployeeId employeeId, String resourceType, int amount);
}
