package com.deskflow.allocation.application.model;

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

import static org.assertj.core.api.Assertions.*;

class AllocationRequestViewTest {

    @Test
    void from_creates_view_with_all_fields() {
        var id = AllocationRequestId.newId();
        var employeeId = EmployeeId.of(UUID.randomUUID());
        var resourceId = ResourceId.of(UUID.randomUUID());
        var dateRange = new DateRange(LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 15));
        var submittedAt = LocalDateTime.of(2025, 6, 1, 10, 0);
        var request = AllocationRequest.reconstitute(id, employeeId, resourceId, dateRange,
                "justification", submittedAt, RequestStatus.SUBMITTED, null, null, null);

        var view = AllocationRequestView.from(request);

        assertThat(view.id()).isEqualTo(id.getValue());
        assertThat(view.employeeId()).isEqualTo(employeeId.getValue());
        assertThat(view.resourceId()).isEqualTo(resourceId.getValue());
        assertThat(view.status()).isEqualTo("SUBMITTED");
        assertThat(view.dateFrom()).isEqualTo(LocalDate.of(2025, 6, 10));
        assertThat(view.dateTo()).isEqualTo(LocalDate.of(2025, 6, 15));
        assertThat(view.justification()).isEqualTo("justification");
        assertThat(view.submittedAt()).isEqualTo(submittedAt);
        assertThat(view.issuedAt()).isNull();
        assertThat(view.returnedAt()).isNull();
        assertThat(view.rejectionReason()).isNull();
    }

    @Test
    void from_creates_view_for_completed_request() {
        var id = AllocationRequestId.newId();
        var employeeId = EmployeeId.of(UUID.randomUUID());
        var resourceId = ResourceId.of(UUID.randomUUID());
        var dateRange = new DateRange(LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 15));
        var submittedAt = LocalDateTime.of(2025, 6, 1, 10, 0);
        var issuedAt = LocalDateTime.of(2025, 6, 2, 9, 0);
        var returnedAt = LocalDateTime.of(2025, 6, 14, 16, 0);
        var request = AllocationRequest.reconstitute(id, employeeId, resourceId, dateRange,
                "test", submittedAt, RequestStatus.COMPLETED, issuedAt, returnedAt, null);

        var view = AllocationRequestView.from(request);

        assertThat(view.status()).isEqualTo("COMPLETED");
        assertThat(view.issuedAt()).isEqualTo(issuedAt);
        assertThat(view.returnedAt()).isEqualTo(returnedAt);
    }

    @Test
    void from_creates_view_for_rejected_request() {
        var id = AllocationRequestId.newId();
        var employeeId = EmployeeId.of(UUID.randomUUID());
        var resourceId = ResourceId.of(UUID.randomUUID());
        var dateRange = new DateRange(LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 15));
        var request = AllocationRequest.reconstitute(id, employeeId, resourceId, dateRange,
                "test", LocalDateTime.now(), RequestStatus.REJECTED, null, null, "Brak miejsca");

        var view = AllocationRequestView.from(request);

        assertThat(view.status()).isEqualTo("REJECTED");
        assertThat(view.rejectionReason()).isEqualTo("Brak miejsca");
    }
}
