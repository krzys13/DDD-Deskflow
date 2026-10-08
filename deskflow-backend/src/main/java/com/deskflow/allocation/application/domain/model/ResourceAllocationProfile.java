package com.deskflow.allocation.application.domain.model;

import com.deskflow.common.application.domain.model.vo.ResourceId;

public record ResourceAllocationProfile(
        ResourceId id,
        String type,
        String name,
        String building,
        int floor,
        String number,
        boolean active,
        boolean available
) {
    public boolean isActive() {
        return active;
    }

    public boolean isAvailable() {
        return available;
    }
}
