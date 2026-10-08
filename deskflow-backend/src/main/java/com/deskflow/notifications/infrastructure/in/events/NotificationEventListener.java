package com.deskflow.notifications.infrastructure.in.events;

import com.deskflow.allocation.application.domain.model.events.AllocationOverdue;
import com.deskflow.allocation.application.domain.model.events.AllocationRequestApproved;
import com.deskflow.allocation.application.domain.model.events.AllocationRequestCancelled;
import com.deskflow.allocation.application.domain.model.events.AllocationRequestRejected;
import com.deskflow.allocation.application.domain.model.events.AllocationRequestSubmitted;
import com.deskflow.allocation.application.domain.model.events.ResourceIssued;
import com.deskflow.allocation.application.domain.model.events.ResourceReturned;
import com.deskflow.notifications.application.port.in.NotificationEventInputPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private final NotificationEventInputPort notificationEventInputPort;

    public NotificationEventListener(NotificationEventInputPort notificationEventInputPort) {
        this.notificationEventInputPort = notificationEventInputPort;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRequestSubmitted(AllocationRequestSubmitted event) {
        notificationEventInputPort.allocationRequestSubmitted(event.requestId(), event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRequestApproved(AllocationRequestApproved event) {
        notificationEventInputPort.allocationRequestApproved(event.requestId(), event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRequestRejected(AllocationRequestRejected event) {
        notificationEventInputPort.allocationRequestRejected(event.requestId(), event.employeeId(), event.reason());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRequestCancelled(AllocationRequestCancelled event) {
        notificationEventInputPort.allocationRequestCancelled(event.requestId(), event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onResourceIssued(ResourceIssued event) {
        notificationEventInputPort.resourceIssued(event.requestId(), event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onResourceReturned(ResourceReturned event) {
        notificationEventInputPort.resourceReturned(event.requestId(), event.employeeId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAllocationOverdue(AllocationOverdue event) {
        notificationEventInputPort.allocationOverdue(event.requestId(), event.employeeId());
    }
}
