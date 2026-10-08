package com.deskflow.common.application.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ResourceTypeTest {

    @Test
    void has_expected_values() {
        assertThat(ResourceType.values()).containsExactly(
                ResourceType.DESK,
                ResourceType.CONFERENCE_ROOM,
                ResourceType.LAPTOP,
                ResourceType.MONITOR,
                ResourceType.PROJECTOR,
                ResourceType.VEHICLE
        );
    }

    @Test
    void valueOf_returns_correct_enum() {
        assertThat(ResourceType.valueOf("DESK")).isEqualTo(ResourceType.DESK);
        assertThat(ResourceType.valueOf("LAPTOP")).isEqualTo(ResourceType.LAPTOP);
    }

    @Test
    void valueOf_invalid_throws() {
        assertThatThrownBy(() -> ResourceType.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
