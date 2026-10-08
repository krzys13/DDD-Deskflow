package com.deskflow.allocation.infrastructure.out.persistence;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.springframework.stereotype.Component;

@Component
public class AllocationRequestMapper {

    AllocationRequestEntity toEntity(AllocationRequest request) {
        AllocationRequestEntity e = new AllocationRequestEntity();
        updateEntity(request, e);
        return e;
    }

    void updateEntity(AllocationRequest request, AllocationRequestEntity e) {
        e.id = request.getId().getValue();
        e.employeeId = request.getEmployeeId().getValue();
        e.resourceId = request.getResourceId().getValue();
        e.status = request.getStatus().name();
        e.dateFrom = request.getDateRange().getDateFrom();
        e.dateTo = request.getDateRange().getDateTo();
        e.justification = request.getJustification();
        e.submittedAt = request.getSubmittedAt();
        e.issuedAt = request.getIssuedAt();
        e.returnedAt = request.getReturnedAt();
        e.rejectionReason = request.getRejectionReason();
    }

    AllocationRequest toDomain(AllocationRequestEntity e) {
        return AllocationRequest.reconstitute(
                AllocationRequestId.of(e.id),
                EmployeeId.of(e.employeeId),
                ResourceId.of(e.resourceId),
                new DateRange(e.dateFrom, e.dateTo),
                e.justification,
                e.submittedAt,
                RequestStatus.valueOf(e.status),
                e.issuedAt,
                e.returnedAt,
                e.rejectionReason
        );
    }
}
