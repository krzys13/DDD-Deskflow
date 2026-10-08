package com.deskflow.employees.application.domain.model;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.employees.application.domain.model.vo.Role;
import com.deskflow.common.application.domain.model.vo.ResourceType;

import java.util.EnumMap;
import java.util.Map;

public class Employee {

    private final EmployeeId id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final Role role;
    private final Map<ResourceType, Integer> limits;

    public Employee(EmployeeId id, String email, String firstName, String lastName,
                    Role role, Map<ResourceType, Integer> limits) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.limits = new EnumMap<>(ResourceType.class);
        this.limits.putAll(limits);
    }

    public int limitForType(ResourceType type) {
        return limits.getOrDefault(type, 1);
    }

    public void changeLimit(ResourceType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Allocation limit cannot be negative");
        }
        limits.put(type, amount);
    }

    public EmployeeId getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }
    public Map<ResourceType, Integer> getLimits() { return Map.copyOf(limits); }
}
