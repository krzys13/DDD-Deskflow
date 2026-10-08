package com.deskflow.allocation.application.domain.service;

import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationLifecycleDomainServicesTest {

    private final ApproveAllocationRequestDomainService approveService = new ApproveAllocationRequestDomainService();
    private final RejectAllocationRequestDomainService rejectService = new RejectAllocationRequestDomainService();
    private final IssueAllocationResourceDomainService issueService = new IssueAllocationResourceDomainService();
    private final ReturnAllocationResourceDomainService returnService = new ReturnAllocationResourceDomainService();
    private final MarkAllocationRequestOverdueDomainService overdueService = new MarkAllocationRequestOverdueDomainService();
    private final CancelAllocationRequestDomainService cancelService = new CancelAllocationRequestDomainService();

    private final EmployeeId employeeId = EmployeeId.of(UUID.randomUUID());

    @Test
    void approve_service_approves_submitted_request() {
        AllocationRequest request = request(RequestStatus.SUBMITTED);

        approveService.approveAllocationRequest(request);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    void reject_service_rejects_request_and_stores_reason() {
        AllocationRequest request = request(RequestStatus.SUBMITTED);

        rejectService.rejectAllocationRequest(request, "No resource available");

        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("No resource available");
    }

    @Test
    void reject_service_rejects_blank_reason() {
        AllocationRequest request = request(RequestStatus.SUBMITTED);

        assertThatThrownBy(() -> rejectService.rejectAllocationRequest(request, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be blank");
    }

    @Test
    void issue_service_issues_approved_request() {
        AllocationRequest request = request(RequestStatus.APPROVED);

        issueService.issueAllocationResource(request);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.ACTIVE);
        assertThat(request.getIssuedAt()).isNotNull();
    }

    @Test
    void return_service_completes_active_request() {
        AllocationRequest request = request(RequestStatus.ACTIVE);

        returnService.returnAllocationResource(request);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(request.getReturnedAt()).isNotNull();
    }

    @Test
    void overdue_service_marks_active_request_as_overdue() {
        AllocationRequest request = request(RequestStatus.ACTIVE);

        overdueService.markAllocationRequestOverdue(request);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.OVERDUE);
    }

    @Test
    void cancel_service_cancels_own_request() {
        AllocationRequest request = request(RequestStatus.SUBMITTED);

        cancelService.cancelAllocationRequest(request, employeeId);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.CANCELLED);
    }

    @Test
    void cancel_service_rejects_cancelling_another_employees_request() {
        AllocationRequest request = request(RequestStatus.SUBMITTED);

        assertThatThrownBy(() -> cancelService.cancelAllocationRequest(request, EmployeeId.of(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another employee");
    }

    private AllocationRequest request(RequestStatus status) {
        return AllocationRequest.reconstitute(
                AllocationRequestId.newId(), employeeId, ResourceId.of(UUID.randomUUID()),
                new DateRange(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5)),
                "test", LocalDateTime.now(), status, null, null, null
        );
    }
}
