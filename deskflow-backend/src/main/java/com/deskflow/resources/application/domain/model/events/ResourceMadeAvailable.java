package com.deskflow.resources.application.domain.model.events;

import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResourceMadeAvailable(
        ResourceId resourceId,
        LocalDateTime occurredAt
) implements DomainEvent {
    @Override
    public UUID eventId() { return UUID.randomUUID(); }
    @Override
    public String eventType() { return "ResourceMadeAvailable"; }
    @Override
    public UUID aggregateId() { return resourceId.getValue(); }
}
