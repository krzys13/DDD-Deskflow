package com.deskflow.notifications.application.port.in;

import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.common.application.domain.model.vo.EmployeeId;

public interface NotificationEventInputPort {
    void allocationRequestSubmitted(AllocationRequestId requestId, EmployeeId employeeId);

    void allocationRequestApproved(AllocationRequestId requestId, EmployeeId employeeId);

    void allocationRequestRejected(AllocationRequestId requestId, EmployeeId employeeId, String reason);

    void allocationRequestCancelled(AllocationRequestId requestId, EmployeeId employeeId);

    void resourceIssued(AllocationRequestId requestId, EmployeeId employeeId);

    void resourceReturned(AllocationRequestId requestId, EmployeeId employeeId);

    void allocationOverdue(AllocationRequestId requestId, EmployeeId employeeId);
}
