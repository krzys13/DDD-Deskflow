package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.common.application.domain.model.vo.EmployeeId;

public class CancelAllocationRequestDomainService {

    public void cancelAllocationRequest(AllocationRequest request, EmployeeId employeeId) {
        if (!request.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Cannot cancel another employee's request");
        }
        request.cancel();
    }
}
