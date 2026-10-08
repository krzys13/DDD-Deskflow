package com.deskflow.allocation.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

interface SpringDataAllocationRequestRepository extends JpaRepository<AllocationRequestEntity, UUID> {

    List<AllocationRequestEntity> findByEmployeeId(UUID employeeId);

    List<AllocationRequestEntity> findByStatus(String status);

    @Query("""
            SELECT w FROM AllocationRequestEntity w
            WHERE w.resourceId = :resourceId
            AND w.status IN ('SUBMITTED', 'APPROVED', 'ACTIVE', 'OVERDUE')
            AND w.dateFrom <= :dateTo
            AND w.dateTo >= :dateFrom
            """)
    List<AllocationRequestEntity> findActiveOverlapping(
            @Param("resourceId") UUID resourceId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    @Query("""
            SELECT w FROM AllocationRequestEntity w
            WHERE w.employeeId = :employeeId
            AND w.status IN ('SUBMITTED', 'APPROVED', 'ACTIVE', 'OVERDUE')
            """)
    List<AllocationRequestEntity> findActiveByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("""
            SELECT w FROM AllocationRequestEntity w
            WHERE w.resourceId = :resourceId
            AND w.status IN ('SUBMITTED', 'APPROVED', 'ACTIVE', 'OVERDUE')
            """)
    List<AllocationRequestEntity> findActiveByResourceId(@Param("resourceId") UUID resourceId);

    @Query("""
            SELECT w FROM AllocationRequestEntity w
            WHERE w.status = 'ACTIVE'
            AND w.dateTo < :now
            """)
    List<AllocationRequestEntity> findOverdue(@Param("now") LocalDate now);
}
