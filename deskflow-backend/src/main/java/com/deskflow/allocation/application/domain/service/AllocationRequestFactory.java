package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.time.LocalDateTime;

public class AllocationRequestFactory {

    public AllocationRequest create(EmployeeId employeeId, ResourceId resourceId,
                                    DateRange dateRange, String justification) {
        return AllocationRequest.create(
                AllocationRequestId.newId(),
                employeeId,
                resourceId,
                dateRange,
                justification,
                LocalDateTime.now()
        );
    }
}
