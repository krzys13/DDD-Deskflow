package com.deskflow.resources.application.domain.model;

import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OfficeResourceTest {

    private OfficeResourceFactory factory;
    private static final Location LOC = new Location("A", 1, "101");

    @BeforeEach
    void setUp() {
        factory = new OfficeResourceFactory();
    }

    @Test
    void new_resource_is_active_and_available() {
        OfficeResource r = factory.create(ResourceType.DESK, "Desk 1", LOC);
        assertThat(r.isActive()).isTrue();
        assertThat(r.isAvailable()).isTrue();
    }

    @Test
    void throws_when_name_blank() {
        assertThatThrownBy(() -> factory.create(ResourceType.DESK, "", LOC))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mark_as_unavailable() {
        OfficeResource r = factory.create(ResourceType.DESK, "Desk 1", LOC);
        r.markAsUnavailable();
        assertThat(r.isAvailable()).isFalse();
        assertThat(r.isActive()).isTrue();
    }

    @Test
    void mark_as_available_after_unavailable() {
        OfficeResource r = factory.create(ResourceType.DESK, "Desk 1", LOC);
        r.markAsUnavailable();
        r.markAsAvailable();
        assertThat(r.isAvailable()).isTrue();
    }

    @Test
    void deactivate_resource() {
        OfficeResource r = factory.create(ResourceType.DESK, "Desk 1", LOC);
        r.deactivate();
        assertThat(r.isActive()).isFalse();
        assertThat(r.isAvailable()).isFalse();
    }

    @Test
    void deactivate_twice_throws() {
        OfficeResource r = factory.create(ResourceType.DESK, "Desk 1", LOC);
        r.deactivate();
        assertThatThrownBy(r::deactivate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void update_changes_name_and_location() {
        OfficeResource r = factory.create(ResourceType.DESK, "Old name", LOC);
        Location newLoc = new Location("B", 3, "305");
        r.update("New name", newLoc);
        assertThat(r.getName()).isEqualTo("New name");
        assertThat(r.getLocation()).isEqualTo(newLoc);
    }

    @Test
    void cannot_mark_unavailable_on_deactivated_resource() {
        OfficeResource r = factory.create(ResourceType.DESK, "Desk", LOC);
        r.deactivate();
        assertThatThrownBy(r::markAsUnavailable).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resource_has_correct_type_and_location() {
        OfficeResource r = factory.create(ResourceType.CONFERENCE_ROOM, "Room A", LOC);
        assertThat(r.getType()).isEqualTo(ResourceType.CONFERENCE_ROOM);
        assertThat(r.getLocation()).isEqualTo(LOC);
    }

    @Test
    void reconstituted_resource_preserves_state() {
        OfficeResource r = OfficeResource.reconstitute(
                ResourceId.of(UUID.randomUUID()), ResourceType.LAPTOP, "Laptop X",
                LOC, false, false
        );
        assertThat(r.isActive()).isFalse();
        assertThat(r.isAvailable()).isFalse();
    }
}
