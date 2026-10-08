package com.deskflow.allocation.application.port.in;

import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;

import java.util.List;
import java.util.Optional;

public interface AllocationAdministrationInputPort {
    Optional<AllocationRequestView> getRequest(AllocationRequestId requestId);

    List<AllocationRequestView> listRequests(RequestStatus status);

    void approveRequest(AllocationRequestId requestId);

    void rejectRequest(AllocationRequestId requestId, String reason);

    void issueResource(AllocationRequestId requestId);

    void returnResource(AllocationRequestId requestId);

    List<AllocationRequestView> monitorActiveAllocations();

    void detectOverdueAllocations();
}
