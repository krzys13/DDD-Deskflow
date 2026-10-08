package com.deskflow.identity.infrastructure.out.employees;

import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.identity.application.model.UserCredentials;
import com.deskflow.identity.application.port.out.UserCredentialsPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeUserCredentialsAdapter implements UserCredentialsPort {

    private final EmployeeManagementInputPort employeeManagementInputPort;

    public EmployeeUserCredentialsAdapter(EmployeeManagementInputPort employeeManagementInputPort) {
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    @Override
    public Optional<UserCredentials> findByEmail(String email) {
        return employeeManagementInputPort.getEmployeeCredentialsByEmail(email)
                .map(credentials -> new UserCredentials(credentials.email(), credentials.passwordHash(), credentials.role()));
    }
}
