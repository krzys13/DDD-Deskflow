package com.deskflow.allocation.infrastructure.in.web;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final EmployeeManagementInputPort employeeManagementInputPort;

    public CurrentUserResolver(EmployeeManagementInputPort employeeManagementInputPort) {
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    public EmployeeId resolveEmployeeId(Authentication auth) {
        return employeeManagementInputPort.getEmployeeByEmail(auth.getName())
                .map(e -> e.getId())
                .orElseThrow(() -> new IllegalStateException("Employee not found for: " + auth.getName()));
    }
}
