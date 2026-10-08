package com.deskflow.employees.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataAllocationLimitRepository extends JpaRepository<AllocationLimitEntity, UUID> {
    Optional<AllocationLimitEntity> findByEmployeeIdAndResourceType(UUID employeeId, String resourceType);
}
