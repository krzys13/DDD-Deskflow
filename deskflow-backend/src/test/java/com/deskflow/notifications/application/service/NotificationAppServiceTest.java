package com.deskflow.notifications.application.service;

import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.employees.application.domain.model.vo.Role;
import com.deskflow.notifications.application.port.out.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationAppServiceTest {

    @Mock private NotificationPort notifications;
    @Mock private EmployeeManagementInputPort employeeManagement;

    private NotificationAppService appService;
    private AllocationRequestId requestId;
    private EmployeeId employeeId;
    private Employee employee;

    @BeforeEach
    void setUp() {
        requestId = AllocationRequestId.newId();
        employeeId = EmployeeId.of(UUID.randomUUID());
        employee = new Employee(employeeId, "test@company.test", "Jan", "Kowalski", Role.EMPLOYEE, java.util.Map.of());
        appService = new NotificationAppService(notifications, employeeManagement);
    }

    @Test
    void submitted_sends_email() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        appService.allocationRequestSubmitted(requestId, employeeId);

        verify(notifications).send("test@company.test", "Request submitted",
                "Your allocation request has been submitted (ID: " + requestId + ").");
    }

    @Test
    void approved_sends_email() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        appService.allocationRequestApproved(requestId, employeeId);

        verify(notifications).send("test@company.test", "Request approved",
                "Your request has been approved (ID: " + requestId + "). Please collect the resource.");
    }

    @Test
    void rejected_sends_email_with_reason() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        appService.allocationRequestRejected(requestId, employeeId, "Brak miejsca");

        verify(notifications).send("test@company.test", "Request rejected",
                "Your request (ID: " + requestId + ") was rejected. Reason: Brak miejsca");
    }

    @Test
    void cancelled_sends_email() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        appService.allocationRequestCancelled(requestId, employeeId);

        verify(notifications).send("test@company.test", "Request cancelled",
                "Your request (ID: " + requestId + ") has been cancelled.");
    }

    @Test
    void resourceIssued_sends_email() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        appService.resourceIssued(requestId, employeeId);

        verify(notifications).send("test@company.test", "Resource issued",
                "The resource from request (ID: " + requestId + ") has been issued to you.");
    }

    @Test
    void resourceReturned_sends_email() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        appService.resourceReturned(requestId, employeeId);

        verify(notifications).send("test@company.test", "Resource returned",
                "We confirm receipt of the resource from request (ID: " + requestId + ").");
    }

    @Test
    void overdue_sends_email() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));

        appService.allocationOverdue(requestId, employeeId);

        verify(notifications).send("test@company.test", "Allocation overdue",
                "Your allocation (ID: " + requestId + ") is past its return date. Please return the resource immediately.");
    }

    @Test
    void employee_not_found_skips_email() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.empty());

        appService.allocationRequestSubmitted(requestId, employeeId);

        verify(notifications, never()).send(any(), any(), any());
    }

    @Test
    void employee_lookup_failure_does_not_propagate() {
        when(employeeManagement.getEmployee(employeeId)).thenThrow(new RuntimeException("DB down"));

        appService.allocationRequestSubmitted(requestId, employeeId);

        verify(notifications, never()).send(any(), any(), any());
    }

    @Test
    void notification_failure_does_not_propagate() {
        when(employeeManagement.getEmployee(employeeId)).thenReturn(Optional.of(employee));
        doThrow(new RuntimeException("SMTP down")).when(notifications).send(any(), any(), any());

        appService.allocationRequestSubmitted(requestId, employeeId);
    }
}
