package com.deskflow.common.application.domain.model.vo;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ResourceIdTest {

    @Test
    void creates_from_uuid() {
        UUID uuid = UUID.randomUUID();
        ResourceId id = ResourceId.of(uuid);
        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    void creates_from_string() {
        UUID uuid = UUID.randomUUID();
        ResourceId id = ResourceId.of(uuid.toString());
        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    void rejects_null() {
        assertThatThrownBy(() -> new ResourceId(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equality() {
        UUID uuid = UUID.randomUUID();
        assertThat(ResourceId.of(uuid)).isEqualTo(ResourceId.of(uuid));
    }

    @Test
    void inequality() {
        assertThat(ResourceId.of(UUID.randomUUID())).isNotEqualTo(ResourceId.of(UUID.randomUUID()));
    }

    @Test
    void toString_returns_uuid_string() {
        UUID uuid = UUID.randomUUID();
        assertThat(ResourceId.of(uuid)).hasToString(uuid.toString());
    }
}
