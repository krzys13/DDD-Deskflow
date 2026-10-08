package com.deskflow.resources.application.domain.model;

import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.resources.application.domain.model.events.ResourceDeactivated;
import com.deskflow.resources.application.domain.model.events.ResourceMadeAvailable;
import com.deskflow.resources.application.domain.model.events.ResourceMadeUnavailable;
import com.deskflow.resources.application.domain.model.events.ResourceUpdated;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OfficeResource {

    private final ResourceId id;
    private final ResourceType type;
    private String name;
    private Location location;
    private boolean active;
    private boolean available;
    private final List<DomainEvent> events = new ArrayList<>();

    private OfficeResource(ResourceId id, ResourceType type, String name, Location location,
                           boolean active, boolean available) {
        this.id = id;
        this.type = type;
        this.name = validateName(name);
        this.location = location;
        this.active = active;
        this.available = available;
    }

    public static OfficeResource create(ResourceId id, ResourceType type, String name, Location location) {
        return new OfficeResource(id, type, name, location, true, true);
    }

    public static OfficeResource reconstitute(ResourceId id, ResourceType type, String name,
                                               Location location, boolean active, boolean available) {
        return new OfficeResource(id, type, name, location, active, available);
    }

    public void update(String newName, Location newLocation) {
        if (!active) throw new IllegalStateException("Cannot edit an inactive resource");
        String prevName = this.name;
        this.name = validateName(newName);
        this.location = newLocation;
        events.add(new ResourceUpdated(id, prevName, newName, LocalDateTime.now()));
    }

    public void markAsUnavailable() {
        if (!active) throw new IllegalStateException("Resource is not active");
        this.available = false;
        events.add(new ResourceMadeUnavailable(id, LocalDateTime.now()));
    }

    public void markAsAvailable() {
        if (!active) throw new IllegalStateException("Resource is not active");
        this.available = true;
        events.add(new ResourceMadeAvailable(id, LocalDateTime.now()));
    }

    public void deactivate() {
        if (!active) throw new IllegalStateException("Resource is already deactivated");
        this.active = false;
        this.available = false;
        events.add(new ResourceDeactivated(id, LocalDateTime.now()));
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Resource name cannot be blank");
        return name;
    }

    public List<DomainEvent> flushEvents() {
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    public ResourceId getId() { return id; }
    public ResourceType getType() { return type; }
    public String getName() { return name; }
    public Location getLocation() { return location; }
    public boolean isActive() { return active; }
    public boolean isAvailable() { return available; }
}
