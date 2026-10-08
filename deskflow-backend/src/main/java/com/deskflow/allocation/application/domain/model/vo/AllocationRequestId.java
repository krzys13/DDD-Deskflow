package com.deskflow.allocation.application.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class AllocationRequestId {
    private final UUID value;

    public AllocationRequestId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static AllocationRequestId of(UUID value) {
        return new AllocationRequestId(value);
    }

    public static AllocationRequestId of(String value) {
        return new AllocationRequestId(UUID.fromString(value));
    }

    public static AllocationRequestId newId() {
        return new AllocationRequestId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllocationRequestId that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
