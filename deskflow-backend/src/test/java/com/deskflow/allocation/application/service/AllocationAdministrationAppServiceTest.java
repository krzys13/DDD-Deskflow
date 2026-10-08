package com.deskflow.allocation.application.service;

import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.events.AllocationRequestApproved;
import com.deskflow.allocation.application.domain.model.events.AllocationRequestRejected;
import com.deskflow.allocation.application.domain.model.events.ResourceIssued;
import com.deskflow.allocation.application.domain.model.events.ResourceReturned;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.allocation.application.domain.service.ApproveAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.IssueAllocationResourceDomainService;
import com.deskflow.allocation.application.domain.service.MarkAllocationRequestOverdueDomainService;
import com.deskflow.allocation.application.domain.service.RejectAllocationRequestDomainService;
import com.deskflow.allocation.application.domain.service.ReturnAllocationResourceDomainService;
import com.deskflow.common.application.port.out.DomainEventPublisherPort;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationAdministrationAppServiceTest {

    @Mock private AllocationRequestRepositoryPort requestRepository;
    @Mock private DomainEventPublisherPort eventPublisher;

    private AllocationAdministrationAppService appService;
    private EmployeeId employeeId;
    private ResourceId resourceId;

    @BeforeEach
    void setUp() {
        appService = new AllocationAdministrationAppService(
                requestRepository,
                eventPublisher,
                new ApproveAllocationRequestDomainService(),
                new RejectAllocationRequestDomainService(),
                new IssueAllocationResourceDomainService(),
                new ReturnAllocationResourceDomainService(),
                new MarkAllocationRequestOverdueDomainService());
        employeeId = EmployeeId.of(UUID.randomUUID());
        resourceId = ResourceId.of(UUID.randomUUID());
    }

    private AllocationRequest createRequest(RequestStatus status) {
        return AllocationRequest.reconstitute(
                AllocationRequestId.newId(), employeeId, resourceId,
                new DateRange(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5)),
                "test", LocalDateTime.now(),
                status, null, null, null
        );
    }

    @Test
    void approve_request_changes_status_and_publishes_event() {
        AllocationRequest request = createRequest(RequestStatus.SUBMITTED);
        when(requestRepository.find(request.getId())).thenReturn(Optional.of(request));

        appService.approveRequest(request.getId());

        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
        verify(requestRepository).save(request);
        verify(eventPublisher).publish(any(AllocationRequestApproved.class));
    }

    @Test
    void reject_request_changes_status_and_publishes_event() {
        AllocationRequest request = createRequest(RequestStatus.SUBMITTED);
        when(requestRepository.find(request.getId())).thenReturn(Optional.of(request));

        appService.rejectRequest(request.getId(), "brak");

        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("brak");
        verify(eventPublisher).publish(any(AllocationRequestRejected.class));
    }

    @Test
    void issue_request_changes_status_and_publishes_event() {
        AllocationRequest request = createRequest(RequestStatus.APPROVED);
        when(requestRepository.find(request.getId())).thenReturn(Optional.of(request));

        appService.issueResource(request.getId());

        assertThat(request.getStatus()).isEqualTo(RequestStatus.ACTIVE);
        verify(eventPublisher).publish(any(ResourceIssued.class));
    }

    @Test
    void return_resource_changes_status_and_publishes_event() {
        AllocationRequest request = createRequest(RequestStatus.ACTIVE);
        when(requestRepository.find(request.getId())).thenReturn(Optional.of(request));

        appService.returnResource(request.getId());

        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        verify(eventPublisher).publish(any(ResourceReturned.class));
    }

    @Test
    void detect_overdue_marks_active_past_due_requests() {
        AllocationRequest request = createRequest(RequestStatus.ACTIVE);
        when(requestRepository.findOverdue(any(LocalDateTime.class))).thenReturn(List.of(request));

        appService.detectOverdueAllocations();

        assertThat(request.getStatus()).isEqualTo(RequestStatus.OVERDUE);
        verify(requestRepository).save(request);
    }

    @Test
    void approve_throws_when_request_missing() {
        AllocationRequestId requestId = AllocationRequestId.newId();
        when(requestRepository.find(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.approveRequest(requestId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Allocation request not found");
    }
}
