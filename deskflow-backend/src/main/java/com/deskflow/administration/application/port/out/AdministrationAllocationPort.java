package com.deskflow.administration.application.port.out;

import com.deskflow.administration.application.model.AdministeredAllocationView;
import com.deskflow.administration.application.domain.model.AdministeredAllocationRequestId;
import com.deskflow.administration.application.domain.model.AdministeredAllocationStatus;

import java.util.List;
import java.util.Optional;

public interface AdministrationAllocationPort {
    Optional<AdministeredAllocationView> getRequest(AdministeredAllocationRequestId requestId);

    List<AdministeredAllocationView> listRequests(AdministeredAllocationStatus status);

    void approveRequest(AdministeredAllocationRequestId requestId);

    void rejectRequest(AdministeredAllocationRequestId requestId, String reason);

    void issueResource(AdministeredAllocationRequestId requestId);

    void returnResource(AdministeredAllocationRequestId requestId);

    List<AdministeredAllocationView> monitorActiveAllocations();

    void detectOverdueAllocations();
}
