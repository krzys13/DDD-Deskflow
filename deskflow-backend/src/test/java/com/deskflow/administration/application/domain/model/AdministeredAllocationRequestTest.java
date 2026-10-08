package com.deskflow.administration.application.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AdministeredAllocationRequestTest {

    private AdministeredAllocationRequestId id = AdministeredAllocationRequestId.of(UUID.randomUUID());

    @Test
    void approve_from_submitted_passes() {
        var req = new AdministeredAllocationRequest(id, AdministeredAllocationStatus.SUBMITTED);

        req.ensureCanApprove();
    }

    @Test
    void approve_from_non_submitted_throws() {
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.APPROVED).ensureCanApprove())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("approve");
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.ACTIVE).ensureCanApprove())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.REJECTED).ensureCanApprove())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.CANCELLED).ensureCanApprove())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.COMPLETED).ensureCanApprove())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.OVERDUE).ensureCanApprove())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reject_from_submitted_passes() {
        var req = new AdministeredAllocationRequest(id, AdministeredAllocationStatus.SUBMITTED);

        req.ensureCanReject();
    }

    @Test
    void reject_from_non_submitted_throws() {
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.APPROVED).ensureCanReject())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reject");
    }

    @Test
    void issue_from_approved_passes() {
        var req = new AdministeredAllocationRequest(id, AdministeredAllocationStatus.APPROVED);

        req.ensureCanIssue();
    }

    @Test
    void issue_from_non_approved_throws() {
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.SUBMITTED).ensureCanIssue())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issue resource");
    }

    @Test
    void return_from_active_passes() {
        var req = new AdministeredAllocationRequest(id, AdministeredAllocationStatus.ACTIVE);

        req.ensureCanReturn();
    }

    @Test
    void return_from_overdue_passes() {
        var req = new AdministeredAllocationRequest(id, AdministeredAllocationStatus.OVERDUE);

        req.ensureCanReturn();
    }

    @Test
    void return_from_non_active_or_overdue_throws() {
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.SUBMITTED).ensureCanReturn())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("return resource");
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.APPROVED).ensureCanReturn())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.COMPLETED).ensureCanReturn())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.REJECTED).ensureCanReturn())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new AdministeredAllocationRequest(id, AdministeredAllocationStatus.CANCELLED).ensureCanReturn())
                .isInstanceOf(IllegalStateException.class);
    }
}
