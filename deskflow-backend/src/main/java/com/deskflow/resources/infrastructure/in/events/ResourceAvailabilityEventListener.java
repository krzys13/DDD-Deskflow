package com.deskflow.resources.infrastructure.in.events;

import com.deskflow.allocation.application.domain.model.events.ResourceIssued;
import com.deskflow.allocation.application.domain.model.events.ResourceReturned;
import com.deskflow.resources.application.port.in.ResourceManagementInputPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ResourceAvailabilityEventListener {

    private final ResourceManagementInputPort resourceManagementInputPort;

    public ResourceAvailabilityEventListener(ResourceManagementInputPort resourceManagementInputPort) {
        this.resourceManagementInputPort = resourceManagementInputPort;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onResourceIssued(ResourceIssued event) {
        resourceManagementInputPort.markUnavailable(event.resourceId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onResourceReturned(ResourceReturned event) {
        resourceManagementInputPort.markAvailable(event.resourceId());
    }
}
