package com.deskflow.common.infrastructure.out.events;

import com.deskflow.common.application.domain.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringDomainEventPublisherAdapterTest {

    @Mock private ApplicationEventPublisher publisher;

    @Test
    void publish_delegates_to_spring_publisher() {
        var adapter = new SpringDomainEventPublisherAdapter(publisher);
        DomainEvent event = mock(DomainEvent.class);

        adapter.publish(event);

        verify(publisher).publishEvent(event);
    }
}
