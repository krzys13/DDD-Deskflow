package com.deskflow.employees.infrastructure.out.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "allocation_limits")
public class AllocationLimitEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "limit_amount", nullable = false)
    private int limitAmount;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public void setEmployee(EmployeeEntity employee) { this.employee = employee; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public int getLimitAmount() { return limitAmount; }
    public void setLimitAmount(int limitAmount) { this.limitAmount = limitAmount; }
}
