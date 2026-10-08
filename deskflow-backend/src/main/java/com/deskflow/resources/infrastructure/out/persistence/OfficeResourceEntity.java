package com.deskflow.resources.infrastructure.out.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "office_resources")
public class OfficeResourceEntity {

    @Id
    private UUID id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "building", nullable = false)
    private String building;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "available", nullable = false)
    private boolean available;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
