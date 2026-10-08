package com.deskflow.employees.application.port.out;

import com.deskflow.employees.application.model.EmployeeCredentials;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.common.application.domain.model.vo.EmployeeId;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepositoryPort {

    Optional<Employee> find(EmployeeId id);

    Optional<Employee> findByEmail(String email);

    Optional<EmployeeCredentials> findCredentialsByEmail(String email);

    void save(Employee employee);

    List<Employee> findAll();
}
