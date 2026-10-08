package com.deskflow.administration.infrastructure.out.allocation;

import com.deskflow.administration.application.model.AdministeredAllocationView;
import com.deskflow.administration.application.port.out.AdministrationAllocationPort;
import com.deskflow.administration.application.domain.model.AdministeredAllocationRequestId;
import com.deskflow.administration.application.domain.model.AdministeredAllocationStatus;
import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.application.port.in.AllocationAdministrationInputPort;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AllocationAdministrationAdapter implements AdministrationAllocationPort {

    private final AllocationAdministrationInputPort allocationAdministrationInputPort;

    public AllocationAdministrationAdapter(AllocationAdministrationInputPort allocationAdministrationInputPort) {
        this.allocationAdministrationInputPort = allocationAdministrationInputPort;
    }

    @Override
    public Optional<AdministeredAllocationView> getRequest(AdministeredAllocationRequestId requestId) {
        return allocationAdministrationInputPort.getRequest(toAllocationId(requestId)).map(this::toAdminView);
    }

    @Override
    public List<AdministeredAllocationView> listRequests(AdministeredAllocationStatus status) {
        RequestStatus allocationStatus = status != null ? RequestStatus.valueOf(status.name()) : null;
        return allocationAdministrationInputPort.listRequests(allocationStatus).stream()
                .map(this::toAdminView)
                .toList();
    }

    @Override
    public void approveRequest(AdministeredAllocationRequestId requestId) {
        allocationAdministrationInputPort.approveRequest(toAllocationId(requestId));
    }

    @Override
    public void rejectRequest(AdministeredAllocationRequestId requestId, String reason) {
        allocationAdministrationInputPort.rejectRequest(toAllocationId(requestId), reason);
    }

    @Override
    public void issueResource(AdministeredAllocationRequestId requestId) {
        allocationAdministrationInputPort.issueResource(toAllocationId(requestId));
    }

    @Override
    public void returnResource(AdministeredAllocationRequestId requestId) {
        allocationAdministrationInputPort.returnResource(toAllocationId(requestId));
    }

    @Override
    public List<AdministeredAllocationView> monitorActiveAllocations() {
        return allocationAdministrationInputPort.monitorActiveAllocations().stream()
                .map(this::toAdminView)
                .toList();
    }

    @Override
    public void detectOverdueAllocations() {
        allocationAdministrationInputPort.detectOverdueAllocations();
    }

    private AllocationRequestId toAllocationId(AdministeredAllocationRequestId requestId) {
        return AllocationRequestId.of(requestId.getValue());
    }

    private AdministeredAllocationView toAdminView(AllocationRequestView view) {
        return new AdministeredAllocationView(
                view.id(),
                view.employeeId(),
                view.resourceId(),
                view.status(),
                view.dateFrom(),
                view.dateTo(),
                view.justification(),
                view.submittedAt(),
                view.issuedAt(),
                view.returnedAt(),
                view.rejectionReason()
        );
    }
}
