package com.deskflow.common.application.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class ResourceId {
    private final UUID value;

    public ResourceId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static ResourceId of(UUID value) {
        return new ResourceId(value);
    }

    public static ResourceId of(String value) {
        return new ResourceId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceId that)) return false;
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
