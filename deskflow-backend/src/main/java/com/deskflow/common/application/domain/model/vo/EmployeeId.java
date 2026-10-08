package com.deskflow.common.application.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class EmployeeId {
    private final UUID value;

    public EmployeeId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static EmployeeId of(UUID value) {
        return new EmployeeId(value);
    }

    public static EmployeeId of(String value) {
        return new EmployeeId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeeId that)) return false;
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
