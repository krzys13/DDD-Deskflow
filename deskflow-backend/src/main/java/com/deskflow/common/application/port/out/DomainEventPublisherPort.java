package com.deskflow.common.application.port.out;

import com.deskflow.common.application.domain.DomainEvent;

public interface DomainEventPublisherPort {
    void publish(DomainEvent event);
}
