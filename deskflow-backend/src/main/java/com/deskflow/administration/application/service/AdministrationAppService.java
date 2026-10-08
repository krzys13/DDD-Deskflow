package com.deskflow.administration.application.service;

import com.deskflow.administration.application.model.AdministeredAllocationView;
import com.deskflow.administration.application.port.in.AdministrationInputPort;
import com.deskflow.administration.application.port.out.AdministrationAllocationPort;
import com.deskflow.administration.application.domain.model.AdministeredAllocationRequest;
import com.deskflow.administration.application.domain.model.AdministeredAllocationRequestId;
import com.deskflow.administration.application.domain.model.AdministeredAllocationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class AdministrationAppService implements AdministrationInputPort {

    private final AdministrationAllocationPort allocationPort;

    public AdministrationAppService(AdministrationAllocationPort allocationPort) {
        this.allocationPort = allocationPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministeredAllocationView> listRequests(AdministeredAllocationStatus status) {
        return allocationPort.listRequests(status);
    }

    @Override
    public void approveRequest(AdministeredAllocationRequestId requestId) {
        loadRequest(requestId).ensureCanApprove();
        allocationPort.approveRequest(requestId);
    }

    @Override
    public void rejectRequest(AdministeredAllocationRequestId requestId, String reason) {
        loadRequest(requestId).ensureCanReject();
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be blank");
        }
        allocationPort.rejectRequest(requestId, reason);
    }

    @Override
    public void issueResource(AdministeredAllocationRequestId requestId) {
        loadRequest(requestId).ensureCanIssue();
        allocationPort.issueResource(requestId);
    }

    @Override
    public void returnResource(AdministeredAllocationRequestId requestId) {
        loadRequest(requestId).ensureCanReturn();
        allocationPort.returnResource(requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministeredAllocationView> monitorActiveAllocations() {
        return allocationPort.monitorActiveAllocations();
    }

    @Override
    public void detectOverdueAllocations() {
        allocationPort.detectOverdueAllocations();
    }

    private AdministeredAllocationRequest loadRequest(AdministeredAllocationRequestId requestId) {
        AdministeredAllocationView view = allocationPort.getRequest(requestId)
                .orElseThrow(() -> new NoSuchElementException("Allocation request not found: " + requestId));
        return new AdministeredAllocationRequest(
                requestId,
                AdministeredAllocationStatus.from(view.status())
        );
    }
}
