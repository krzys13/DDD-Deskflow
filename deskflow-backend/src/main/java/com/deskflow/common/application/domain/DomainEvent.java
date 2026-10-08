package com.deskflow.common.application.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    String eventType();
    UUID aggregateId();
    LocalDateTime occurredAt();
}
