package com.deskflow.resources.application.domain.model.vo;

import java.util.Objects;

public final class Location {
    private final String building;
    private final int floor;
    private final String number;

    public Location(String building, int floor, String number) {
        if (building == null || building.isBlank()) throw new IllegalArgumentException("Building cannot be blank");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("Number cannot be blank");
        this.building = building;
        this.floor = floor;
        this.number = number;
    }

    public String getBuilding() { return building; }
    public int getFloor() { return floor; }
    public String getNumber() { return number; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location that)) return false;
        return floor == that.floor && building.equals(that.building) && number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(building, floor, number);
    }

    @Override
    public String toString() {
        return building + ", floor " + floor + ", no. " + number;
    }
}
