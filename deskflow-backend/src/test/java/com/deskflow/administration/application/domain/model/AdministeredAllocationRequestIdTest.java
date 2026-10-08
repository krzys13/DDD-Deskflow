package com.deskflow.administration.application.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AdministeredAllocationRequestIdTest {

    @Test
    void creates_from_uuid() {
        UUID uuid = UUID.randomUUID();
        var id = AdministeredAllocationRequestId.of(uuid);
        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    void rejects_null() {
        assertThatThrownBy(() -> AdministeredAllocationRequestId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equality() {
        UUID uuid = UUID.randomUUID();
        assertThat(AdministeredAllocationRequestId.of(uuid)).isEqualTo(AdministeredAllocationRequestId.of(uuid));
    }

    @Test
    void inequality() {
        assertThat(AdministeredAllocationRequestId.of(UUID.randomUUID()))
                .isNotEqualTo(AdministeredAllocationRequestId.of(UUID.randomUUID()));
    }

    @Test
    void toString_returns_uuid_string() {
        UUID uuid = UUID.randomUUID();
        assertThat(AdministeredAllocationRequestId.of(uuid)).hasToString(uuid.toString());
    }
}
