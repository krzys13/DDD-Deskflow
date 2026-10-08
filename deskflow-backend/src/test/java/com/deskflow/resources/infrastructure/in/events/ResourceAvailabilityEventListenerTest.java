package com.deskflow.resources.infrastructure.in.events;

import com.deskflow.allocation.application.domain.model.events.ResourceIssued;
import com.deskflow.allocation.application.domain.model.events.ResourceReturned;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.resources.application.port.in.ResourceManagementInputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceAvailabilityEventListenerTest {

    @Mock private ResourceManagementInputPort resourceManagementInputPort;

    private ResourceAvailabilityEventListener listener;
    private ResourceId resourceId;

    @BeforeEach
    void setUp() {
        listener = new ResourceAvailabilityEventListener(resourceManagementInputPort);
        resourceId = ResourceId.of(UUID.randomUUID());
    }

    @Test
    void onResourceIssued_marks_resource_unavailable() {
        var event = new ResourceIssued(AllocationRequestId.newId(), EmployeeId.of(UUID.randomUUID()), resourceId, LocalDateTime.now());

        listener.onResourceIssued(event);

        verify(resourceManagementInputPort).markUnavailable(resourceId);
    }

    @Test
    void onResourceReturned_marks_resource_available() {
        var event = new ResourceReturned(AllocationRequestId.newId(), EmployeeId.of(UUID.randomUUID()), resourceId, LocalDateTime.now());

        listener.onResourceReturned(event);

        verify(resourceManagementInputPort).markAvailable(resourceId);
    }
}
