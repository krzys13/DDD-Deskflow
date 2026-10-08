package com.deskflow.allocation.application.port.out;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AllocationRequestRepositoryPort {

    void save(AllocationRequest request);

    Optional<AllocationRequest> find(AllocationRequestId id);

    List<AllocationRequest> findActiveForResource(ResourceId resourceId, DateRange dateRange);

    List<AllocationRequest> findActiveForResource(ResourceId resourceId);

    List<AllocationRequest> findActiveForEmployee(EmployeeId employeeId);

    List<AllocationRequest> findOverdue(LocalDateTime now);

    List<AllocationRequest> findForEmployee(EmployeeId employeeId);

    List<AllocationRequest> findAll(RequestStatus status);

    List<AllocationRequest> findAll();
}
