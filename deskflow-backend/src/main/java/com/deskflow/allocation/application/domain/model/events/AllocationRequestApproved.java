package com.deskflow.allocation.application.domain.model.events;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;

import java.time.LocalDateTime;
import java.util.UUID;

public record AllocationRequestApproved(
        AllocationRequestId requestId,
        EmployeeId employeeId,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public UUID eventId() { return UUID.randomUUID(); }
    @Override
    public String eventType() { return "AllocationRequestApproved"; }
    @Override
    public UUID aggregateId() { return requestId.getValue(); }
}
