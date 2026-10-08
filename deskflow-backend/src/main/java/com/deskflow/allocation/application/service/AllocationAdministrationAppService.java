package com.deskflow.allocation.application.service;

import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.application.port.in.AllocationAdministrationInputPort;
import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.allocation.application.domain.service.ApproveAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.IssueAllocationResourceDomainService;
import com.deskflow.allocation.application.domain.service.MarkAllocationRequestOverdueDomainService;
import com.deskflow.allocation.application.domain.service.RejectAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.ReturnAllocationResourceDomainService;
import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import com.deskflow.common.application.domain.DomainEvent;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class AllocationAdministrationAppService implements AllocationAdministrationInputPort {

    private final AllocationRequestRepositoryPort requestRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final ApproveAllocationRequestDomainService approveAllocationRequestDomainService;
    private final RejectAllocationRequestDomainService rejectAllocationRequestDomainService;
    private final IssueAllocationResourceDomainService issueAllocationResourceDomainService;
    private final ReturnAllocationResourceDomainService returnAllocationResourceDomainService;
    private final MarkAllocationRequestOverdueDomainService markAllocationRequestOverdueDomainService;

    public AllocationAdministrationAppService(AllocationRequestRepositoryPort requestRepository,
                                              DomainEventPublisherPort eventPublisher,
                                              ApproveAllocationRequestDomainService approveAllocationRequestDomainService,
                                              RejectAllocationRequestDomainService rejectAllocationRequestDomainService,
                                              IssueAllocationResourceDomainService issueAllocationResourceDomainService,
                                              ReturnAllocationResourceDomainService returnAllocationResourceDomainService,
                                              MarkAllocationRequestOverdueDomainService markAllocationRequestOverdueDomainService) {
        this.requestRepository = requestRepository;
        this.eventPublisher = eventPublisher;
        this.approveAllocationRequestDomainService = approveAllocationRequestDomainService;
        this.rejectAllocationRequestDomainService = rejectAllocationRequestDomainService;
        this.issueAllocationResourceDomainService = issueAllocationResourceDomainService;
        this.returnAllocationResourceDomainService = returnAllocationResourceDomainService;
        this.markAllocationRequestOverdueDomainService = markAllocationRequestOverdueDomainService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AllocationRequestView> getRequest(AllocationRequestId requestId) {
        return requestRepository.find(requestId).map(AllocationRequestView::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationRequestView> listRequests(RequestStatus status) {
        List<AllocationRequest> requests = status == null ? requestRepository.findAll() : requestRepository.findAll(status);
        return requests.stream().map(AllocationRequestView::from).toList();
    }

    @Override
    public void approveRequest(AllocationRequestId requestId) {
        mutate(requestId, approveAllocationRequestDomainService::approveAllocationRequest);
    }

    @Override
    public void rejectRequest(AllocationRequestId requestId, String reason) {
        mutate(requestId, request -> rejectAllocationRequestDomainService.rejectAllocationRequest(request, reason));
    }

    @Override
    public void issueResource(AllocationRequestId requestId) {
        mutate(requestId, issueAllocationResourceDomainService::issueAllocationResource);
    }

    @Override
    public void returnResource(AllocationRequestId requestId) {
        mutate(requestId, returnAllocationResourceDomainService::returnAllocationResource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationRequestView> monitorActiveAllocations() {
        return requestRepository.findAll(RequestStatus.ACTIVE).stream()
                .map(AllocationRequestView::from)
                .toList();
    }

    @Override
    public void detectOverdueAllocations() {
        List<AllocationRequest> overdue = requestRepository.findOverdue(LocalDateTime.now());
        for (AllocationRequest request : overdue) {
            markAllocationRequestOverdueDomainService.markAllocationRequestOverdue(request);
            requestRepository.save(request);
            publishAndClear(request);
        }
    }

    private void mutate(AllocationRequestId requestId, java.util.function.Consumer<AllocationRequest> mutation) {
        AllocationRequest request = requestRepository.find(requestId)
                .orElseThrow(() -> new NoSuchElementException("Allocation request not found: " + requestId));
        mutation.accept(request);
        requestRepository.save(request);
        publishAndClear(request);
    }

    private void publishAndClear(AllocationRequest request) {
        for (DomainEvent event : request.flushEvents()) {
            eventPublisher.publish(event);
        }
    }
}
