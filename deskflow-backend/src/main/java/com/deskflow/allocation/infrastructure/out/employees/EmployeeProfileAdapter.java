package com.deskflow.allocation.infrastructure.out.employees;

import com.deskflow.allocation.application.domain.model.EmployeeAllocationProfile;
import com.deskflow.allocation.application.port.out.EmployeeProfilePort;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmployeeProfileAdapter implements EmployeeProfilePort {

    private final EmployeeManagementInputPort employeeManagementInputPort;

    public EmployeeProfileAdapter(EmployeeManagementInputPort employeeManagementInputPort) {
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    @Override
    public Optional<EmployeeAllocationProfile> findEmployee(EmployeeId employeeId) {
        return employeeManagementInputPort.getEmployee(employeeId)
                .map(employee -> new EmployeeAllocationProfile(
                        employee.getLimits().entrySet().stream()
                                .collect(Collectors.toMap(entry -> entry.getKey().name(), java.util.Map.Entry::getValue))
                ));
    }
}
