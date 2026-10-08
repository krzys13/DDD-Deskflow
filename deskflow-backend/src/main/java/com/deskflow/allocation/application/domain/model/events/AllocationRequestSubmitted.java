package com.deskflow.allocation.application.domain.model.events;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.time.LocalDateTime;
import java.util.UUID;

public record AllocationRequestSubmitted(
        AllocationRequestId requestId,
        EmployeeId employeeId,
        ResourceId resourceId,
        DateRange dateRange,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public UUID eventId() { return UUID.randomUUID(); }
    @Override
    public String eventType() { return "AllocationRequestSubmitted"; }
    @Override
    public UUID aggregateId() { return requestId.getValue(); }
}
