package com.deskflow.notifications.infrastructure.in.events;

import com.deskflow.allocation.application.domain.model.events.*;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.notifications.application.port.in.NotificationEventInputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock private NotificationEventInputPort notificationInputPort;

    private NotificationEventListener listener;
    private AllocationRequestId requestId;
    private EmployeeId employeeId;

    private ResourceId resourceId;
    private DateRange dateRange;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        listener = new NotificationEventListener(notificationInputPort);
        requestId = AllocationRequestId.newId();
        employeeId = EmployeeId.of(UUID.randomUUID());
        resourceId = ResourceId.of(UUID.randomUUID());
        dateRange = new DateRange(LocalDate.now(), LocalDate.now().plusDays(1));
        now = LocalDateTime.now();
    }

    @Test
    void onRequestSubmitted_delegates() {
        var event = new AllocationRequestSubmitted(requestId, employeeId, resourceId, dateRange, now);

        listener.onRequestSubmitted(event);

        verify(notificationInputPort).allocationRequestSubmitted(requestId, employeeId);
    }

    @Test
    void onRequestApproved_delegates() {
        var event = new AllocationRequestApproved(requestId, employeeId, now);

        listener.onRequestApproved(event);

        verify(notificationInputPort).allocationRequestApproved(requestId, employeeId);
    }

    @Test
    void onRequestRejected_delegates() {
        var event = new AllocationRequestRejected(requestId, employeeId, "No space", now);

        listener.onRequestRejected(event);

        verify(notificationInputPort).allocationRequestRejected(requestId, employeeId, "No space");
    }

    @Test
    void onRequestCancelled_delegates() {
        var event = new AllocationRequestCancelled(requestId, employeeId, now);

        listener.onRequestCancelled(event);

        verify(notificationInputPort).allocationRequestCancelled(requestId, employeeId);
    }

    @Test
    void onResourceIssued_delegates() {
        var event = new ResourceIssued(requestId, employeeId, resourceId, now);

        listener.onResourceIssued(event);

        verify(notificationInputPort).resourceIssued(requestId, employeeId);
    }

    @Test
    void onResourceReturned_delegates() {
        var event = new ResourceReturned(requestId, employeeId, resourceId, now);

        listener.onResourceReturned(event);

        verify(notificationInputPort).resourceReturned(requestId, employeeId);
    }

    @Test
    void onAllocationOverdue_delegates() {
        var event = new AllocationOverdue(requestId, employeeId, resourceId, now);

        listener.onAllocationOverdue(event);

        verify(notificationInputPort).allocationOverdue(requestId, employeeId);
    }
}
