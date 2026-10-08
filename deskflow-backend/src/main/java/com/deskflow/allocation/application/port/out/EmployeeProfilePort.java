package com.deskflow.allocation.application.port.out;

import com.deskflow.allocation.application.domain.model.EmployeeAllocationProfile;
import com.deskflow.common.application.domain.model.vo.EmployeeId;

import java.util.Optional;

public interface EmployeeProfilePort {
    Optional<EmployeeAllocationProfile> findEmployee(EmployeeId employeeId);
}
