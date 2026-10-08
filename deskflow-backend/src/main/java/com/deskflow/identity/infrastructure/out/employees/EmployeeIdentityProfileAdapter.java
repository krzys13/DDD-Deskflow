package com.deskflow.identity.infrastructure.out.employees;

import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.identity.application.model.IdentityProfile;
import com.deskflow.identity.application.port.out.IdentityEmployeeProfilePort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeIdentityProfileAdapter implements IdentityEmployeeProfilePort {

    private final EmployeeManagementInputPort employeeManagementInputPort;

    public EmployeeIdentityProfileAdapter(EmployeeManagementInputPort employeeManagementInputPort) {
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    @Override
    public Optional<IdentityProfile> findByEmail(String email) {
        return employeeManagementInputPort.getEmployeeByEmail(email)
                .map(employee -> new IdentityProfile(
                        employee.getEmail(),
                        employee.getRole().name(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getId().toString()
                ));
    }
}
