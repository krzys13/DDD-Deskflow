package com.deskflow.common.application.domain.model.vo;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class EmployeeIdTest {

    @Test
    void creates_from_uuid() {
        UUID uuid = UUID.randomUUID();
        EmployeeId id = EmployeeId.of(uuid);
        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    void creates_from_string() {
        UUID uuid = UUID.randomUUID();
        EmployeeId id = EmployeeId.of(uuid.toString());
        assertThat(id.getValue()).isEqualTo(uuid);
    }

    @Test
    void rejects_null() {
        assertThatThrownBy(() -> new EmployeeId(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejects_null_via_factory() {
        assertThatThrownBy(() -> EmployeeId.of((UUID) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equality() {
        UUID uuid = UUID.randomUUID();
        assertThat(EmployeeId.of(uuid)).isEqualTo(EmployeeId.of(uuid));
    }

    @Test
    void inequality() {
        assertThat(EmployeeId.of(UUID.randomUUID())).isNotEqualTo(EmployeeId.of(UUID.randomUUID()));
    }

    @Test
    void hashCode_is_consistent() {
        UUID uuid = UUID.randomUUID();
        assertThat(EmployeeId.of(uuid)).hasSameHashCodeAs(EmployeeId.of(uuid));
    }

    @Test
    void toString_returns_uuid_string() {
        UUID uuid = UUID.randomUUID();
        assertThat(EmployeeId.of(uuid)).hasToString(uuid.toString());
    }
}
