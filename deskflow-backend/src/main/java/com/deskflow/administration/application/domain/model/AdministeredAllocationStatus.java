package com.deskflow.administration.application.domain.model;

public enum AdministeredAllocationStatus {
    SUBMITTED,
    APPROVED,
    ACTIVE,
    OVERDUE,
    REJECTED,
    CANCELLED,
    COMPLETED;

    public static AdministeredAllocationStatus from(String status) {
        return AdministeredAllocationStatus.valueOf(status);
    }
}
