package com.deskflow.employees.infrastructure.out.persistence;

import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.employees.application.domain.model.vo.Role;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmployeeMapper {

    public Employee toDomain(EmployeeEntity e) {
        Map<ResourceType, Integer> limits = e.getAllocationLimits().stream()
                .collect(Collectors.toMap(
                        l -> ResourceType.valueOf(l.getResourceType()),
                        AllocationLimitEntity::getLimitAmount
                ));
        return new Employee(
                EmployeeId.of(e.getId()),
                e.getEmail(), e.getFirstName(), e.getLastName(),
                Role.valueOf(e.getRole()),
                limits
        );
    }
}
