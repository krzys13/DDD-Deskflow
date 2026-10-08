package com.deskflow.administration.application.domain.model;

public record AdministeredAllocationRequest(
        AdministeredAllocationRequestId id,
        AdministeredAllocationStatus status
) {
    public void ensureCanApprove() {
        requireStatus(AdministeredAllocationStatus.SUBMITTED, "approve");
    }

    public void ensureCanReject() {
        requireStatus(AdministeredAllocationStatus.SUBMITTED, "reject");
    }

    public void ensureCanIssue() {
        requireStatus(AdministeredAllocationStatus.APPROVED, "issue resource");
    }

    public void ensureCanReturn() {
        if (status != AdministeredAllocationStatus.ACTIVE && status != AdministeredAllocationStatus.OVERDUE) {
            throw new IllegalStateException(
                    "Operation 'return resource' requires ACTIVE or OVERDUE status, current: " + status);
        }
    }

    private void requireStatus(AdministeredAllocationStatus required, String operation) {
        if (status != required) {
            throw new IllegalStateException(
                    "Operation '" + operation + "' requires status " + required + ", current: " + status);
        }
    }
}
