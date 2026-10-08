package com.deskflow.allocation.infrastructure.out.persistence;

import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class HibernateAllocationRequestRepositoryAdapter implements AllocationRequestRepositoryPort {

    private final SpringDataAllocationRequestRepository jpa;
    private final AllocationRequestMapper mapper;

    public HibernateAllocationRequestRepositoryAdapter(SpringDataAllocationRequestRepository jpa, AllocationRequestMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public void save(AllocationRequest request) {
        jpa.findById(request.getId().getValue()).ifPresentOrElse(
                managed -> mapper.updateEntity(request, managed),
                () -> jpa.save(mapper.toEntity(request))
        );
    }

    @Override
    public Optional<AllocationRequest> find(AllocationRequestId id) {
        return jpa.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<AllocationRequest> findActiveForResource(ResourceId resourceId, DateRange dateRange) {
        return jpa.findActiveOverlapping(resourceId.getValue(), dateRange.getDateFrom(), dateRange.getDateTo())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AllocationRequest> findActiveForResource(ResourceId resourceId) {
        return jpa.findActiveByResourceId(resourceId.getValue())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AllocationRequest> findActiveForEmployee(EmployeeId employeeId) {
        return jpa.findActiveByEmployeeId(employeeId.getValue())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AllocationRequest> findOverdue(LocalDateTime now) {
        return jpa.findOverdue(now.toLocalDate())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AllocationRequest> findForEmployee(EmployeeId employeeId) {
        return jpa.findByEmployeeId(employeeId.getValue())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AllocationRequest> findAll(RequestStatus status) {
        return jpa.findByStatus(status.name())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AllocationRequest> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }
}
