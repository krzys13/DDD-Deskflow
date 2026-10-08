package com.deskflow.administration.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class AdministeredAllocationRequestId {
    private final UUID value;

    private AdministeredAllocationRequestId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static AdministeredAllocationRequestId of(UUID value) {
        return new AdministeredAllocationRequestId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdministeredAllocationRequestId that)) return false;
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
