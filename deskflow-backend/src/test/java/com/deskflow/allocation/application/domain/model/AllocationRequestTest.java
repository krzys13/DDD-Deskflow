package com.deskflow.allocation.application.domain.model;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.events.*;
import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AllocationRequestTest {

    private static final EmployeeId EMPLOYEE_ID = EmployeeId.of(UUID.randomUUID());
    private static final ResourceId RESOURCE_ID = ResourceId.of(UUID.randomUUID());
    private static final DateRange DATE_RANGE = new DateRange(
            LocalDate.of(2025, 6, 10),
            LocalDate.of(2025, 6, 15)
    );

    private AllocationRequest request;

    @BeforeEach
    void setUp() {
        AllocationRequestFactory factory = new AllocationRequestFactory();
        request = factory.create(EMPLOYEE_ID, RESOURCE_ID, DATE_RANGE, "Potrzebuję do projektu");
    }

    @Test
    void new_request_has_submitted_status() {
        assertThat(request.getStatus()).isEqualTo(RequestStatus.SUBMITTED);
    }

    @Test
    void new_request_publishes_submitted_event() {
        assertThat(request.flushEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(AllocationRequestSubmitted.class);
    }

    @Test
    void approve_changes_status_to_approved() {
        request.flushEvents();
        request.approve();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    void approve_publishes_approved_event() {
        request.flushEvents();
        request.approve();
        assertThat(request.flushEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(AllocationRequestApproved.class);
    }

    @Test
    void reject_changes_status_and_sets_reason() {
        request.reject("Brak dostępności");
        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("Brak dostępności");
    }

    @Test
    void reject_publishes_rejected_event() {
        request.flushEvents();
        request.reject("powód");
        assertThat(request.flushEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(AllocationRequestRejected.class);
    }

    @Test
    void cancel_from_submitted_changes_status() {
        request.cancel();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.CANCELLED);
    }

    @Test
    void cancel_from_approved_changes_status() {
        request.approve();
        request.cancel();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.CANCELLED);
    }

    @Test
    void cancel_from_active_throws() {
        request.approve();
        request.issueResource();
        assertThatThrownBy(request::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void issue_resource_changes_status_to_active() {
        request.approve();
        request.issueResource();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.ACTIVE);
        assertThat(request.getIssuedAt()).isNotNull();
    }

    @Test
    void issue_resource_from_submitted_throws() {
        assertThatThrownBy(request::issueResource)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void return_resource_from_active_changes_status_to_completed() {
        request.approve();
        request.issueResource();
        request.returnResource();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(request.getReturnedAt()).isNotNull();
    }

    @Test
    void return_resource_from_overdue_changes_status_to_completed() {
        request.approve();
        request.issueResource();
        request.markAsOverdue();
        request.returnResource();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    void mark_as_overdue_from_active_changes_status() {
        request.approve();
        request.issueResource();
        request.markAsOverdue();
        assertThat(request.getStatus()).isEqualTo(RequestStatus.OVERDUE);
    }

    @Test
    void mark_as_overdue_from_submitted_throws() {
        assertThatThrownBy(request::markAsOverdue)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void approve_after_rejection_throws() {
        request.reject("za późno");
        assertThatThrownBy(request::approve)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void approve_after_cancellation_throws() {
        request.cancel();
        assertThatThrownBy(request::approve)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void request_stores_input_data() {
        assertThat(request.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(request.getResourceId()).isEqualTo(RESOURCE_ID);
        assertThat(request.getDateRange()).isEqualTo(DATE_RANGE);
        assertThat(request.getJustification()).isEqualTo("Potrzebuję do projektu");
        assertThat(request.getSubmittedAt()).isNotNull();
    }

    @Test
    void reconstituted_request_has_no_events() {
        AllocationRequest reconstituted = AllocationRequest.reconstitute(
                AllocationRequestId.newId(), EMPLOYEE_ID, RESOURCE_ID, DATE_RANGE,
                "uzasadnienie", LocalDateTime.now(),
                RequestStatus.APPROVED, null, null, null
        );
        assertThat(reconstituted.flushEvents()).isEmpty();
        assertThat(reconstituted.getStatus()).isEqualTo(RequestStatus.APPROVED);
    }
}
