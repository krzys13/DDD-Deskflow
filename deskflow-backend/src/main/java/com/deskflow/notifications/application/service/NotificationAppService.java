package com.deskflow.notifications.application.service;

import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.notifications.application.port.in.NotificationEventInputPort;
import com.deskflow.notifications.application.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NotificationAppService implements NotificationEventInputPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationAppService.class);

    private final NotificationPort notifications;
    private final EmployeeManagementInputPort employeeManagementInputPort;

    public NotificationAppService(NotificationPort notifications,
                                  EmployeeManagementInputPort employeeManagementInputPort) {
        this.notifications = notifications;
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    @Override
    public void allocationRequestSubmitted(AllocationRequestId requestId, EmployeeId employeeId) {
        emailEmployee(employeeId,
                "Request submitted",
                "Your allocation request has been submitted (ID: " + requestId + ").");
    }

    @Override
    public void allocationRequestApproved(AllocationRequestId requestId, EmployeeId employeeId) {
        emailEmployee(employeeId,
                "Request approved",
                "Your request has been approved (ID: " + requestId + "). Please collect the resource.");
    }

    @Override
    public void allocationRequestRejected(AllocationRequestId requestId, EmployeeId employeeId, String reason) {
        emailEmployee(employeeId,
                "Request rejected",
                "Your request (ID: " + requestId + ") was rejected. Reason: " + reason);
    }

    @Override
    public void allocationRequestCancelled(AllocationRequestId requestId, EmployeeId employeeId) {
        emailEmployee(employeeId,
                "Request cancelled",
                "Your request (ID: " + requestId + ") has been cancelled.");
    }

    @Override
    public void resourceIssued(AllocationRequestId requestId, EmployeeId employeeId) {
        emailEmployee(employeeId,
                "Resource issued",
                "The resource from request (ID: " + requestId + ") has been issued to you.");
    }

    @Override
    public void resourceReturned(AllocationRequestId requestId, EmployeeId employeeId) {
        emailEmployee(employeeId,
                "Resource returned",
                "We confirm receipt of the resource from request (ID: " + requestId + ").");
    }

    @Override
    public void allocationOverdue(AllocationRequestId requestId, EmployeeId employeeId) {
        emailEmployee(employeeId,
                "Allocation overdue",
                "Your allocation (ID: " + requestId + ") is past its return date. Please return the resource immediately.");
    }

    private void emailEmployee(EmployeeId employeeId, String subject, String body) {
        try {
            employeeManagementInputPort.getEmployee(employeeId)
                    .ifPresent(employee -> notifications.send(employee.getEmail(), subject, body));
        } catch (Exception e) {
            log.warn("Failed to send notification to employee {}: {}", employeeId, e.getMessage(), e);
        }
    }
}
