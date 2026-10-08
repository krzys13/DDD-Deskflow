package com.deskflow.resources.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOfficeResourceRepository extends JpaRepository<OfficeResourceEntity, UUID> {
    List<OfficeResourceEntity> findByActiveTrue();
}
