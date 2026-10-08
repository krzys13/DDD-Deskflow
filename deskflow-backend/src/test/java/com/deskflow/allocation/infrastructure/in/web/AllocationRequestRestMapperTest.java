package com.deskflow.allocation.infrastructure.in.web;

import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.infrastructure.in.web.dto.AllocationRequestResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AllocationRequestRestMapperTest {

    private final UUID id = UUID.randomUUID();
    private final UUID employeeId = UUID.randomUUID();
    private final UUID resourceId = UUID.randomUUID();
    private final LocalDate dateFrom = LocalDate.of(2025, 6, 10);
    private final LocalDate dateTo = LocalDate.of(2025, 6, 15);
    private final LocalDateTime submittedAt = LocalDateTime.of(2025, 6, 1, 10, 0);

    @Test
    void toResponse_maps_all_fields() {
        var view = new AllocationRequestView(id, employeeId, resourceId, "SUBMITTED",
                dateFrom, dateTo, "justification", submittedAt, null, null, null);

        var response = AllocationRequestRestMapper.toResponse(view);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.employeeId()).isEqualTo(employeeId);
        assertThat(response.resourceId()).isEqualTo(resourceId);
        assertThat(response.status()).isEqualTo("SUBMITTED");
        assertThat(response.dateFrom()).isEqualTo(dateFrom);
        assertThat(response.dateTo()).isEqualTo(dateTo);
        assertThat(response.justification()).isEqualTo("justification");
        assertThat(response.submittedAt()).isEqualTo(submittedAt);
        assertThat(response.issuedAt()).isNull();
        assertThat(response.returnedAt()).isNull();
        assertThat(response.rejectionReason()).isNull();
        assertThat(response.employeeFirstName()).isNull();
        assertThat(response.employeeLastName()).isNull();
    }

    @Test
    void toResponse_with_employee_names() {
        var view = new AllocationRequestView(id, employeeId, resourceId, "APPROVED",
                dateFrom, dateTo, "test", submittedAt, LocalDateTime.now(), null, null);

        var response = AllocationRequestRestMapper.toResponse(view, "Anna", "Kowalska");

        assertThat(response.employeeFirstName()).isEqualTo("Anna");
        assertThat(response.employeeLastName()).isEqualTo("Kowalska");
    }

    @Test
    void toResponse_maps_completed_request_with_all_fields() {
        var issuedAt = LocalDateTime.of(2025, 6, 11, 9, 0);
        var returnedAt = LocalDateTime.of(2025, 6, 14, 16, 0);
        var view = new AllocationRequestView(id, employeeId, resourceId, "COMPLETED",
                dateFrom, dateTo, "projekt", submittedAt, issuedAt, returnedAt, null);

        var response = AllocationRequestRestMapper.toResponse(view, "Jan", "Test");

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.issuedAt()).isEqualTo(issuedAt);
        assertThat(response.returnedAt()).isEqualTo(returnedAt);
        assertThat(response.employeeFirstName()).isEqualTo("Jan");
        assertThat(response.employeeLastName()).isEqualTo("Test");
    }

    @Test
    void toResponse_maps_rejected_request() {
        var view = new AllocationRequestView(id, employeeId, resourceId, "REJECTED",
                dateFrom, dateTo, "test", submittedAt, null, null, "Brak miejsca");

        var response = AllocationRequestRestMapper.toResponse(view);

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(response.rejectionReason()).isEqualTo("Brak miejsca");
    }
}
