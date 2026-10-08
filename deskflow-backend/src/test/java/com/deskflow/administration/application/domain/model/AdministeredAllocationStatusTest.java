package com.deskflow.administration.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AdministeredAllocationStatusTest {

    @Test
    void from_returns_correct_enum() {
        assertThat(AdministeredAllocationStatus.from("SUBMITTED")).isEqualTo(AdministeredAllocationStatus.SUBMITTED);
        assertThat(AdministeredAllocationStatus.from("APPROVED")).isEqualTo(AdministeredAllocationStatus.APPROVED);
        assertThat(AdministeredAllocationStatus.from("ACTIVE")).isEqualTo(AdministeredAllocationStatus.ACTIVE);
        assertThat(AdministeredAllocationStatus.from("OVERDUE")).isEqualTo(AdministeredAllocationStatus.OVERDUE);
        assertThat(AdministeredAllocationStatus.from("REJECTED")).isEqualTo(AdministeredAllocationStatus.REJECTED);
        assertThat(AdministeredAllocationStatus.from("CANCELLED")).isEqualTo(AdministeredAllocationStatus.CANCELLED);
        assertThat(AdministeredAllocationStatus.from("COMPLETED")).isEqualTo(AdministeredAllocationStatus.COMPLETED);
    }

    @Test
    void from_invalid_throws() {
        assertThatThrownBy(() -> AdministeredAllocationStatus.from("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
