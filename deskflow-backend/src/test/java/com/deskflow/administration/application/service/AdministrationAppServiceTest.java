package com.deskflow.administration.application.service;

import com.deskflow.administration.application.port.out.AdministrationAllocationPort;
import com.deskflow.administration.application.model.AdministeredAllocationView;
import com.deskflow.administration.application.domain.model.AdministeredAllocationRequestId;
import com.deskflow.administration.application.domain.model.AdministeredAllocationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrationAppServiceTest {

    @Mock private AdministrationAllocationPort allocationPort;

    private AdministrationAppService appService;

    @BeforeEach
    void setUp() {
        appService = new AdministrationAppService(allocationPort);
    }

    private AdministeredAllocationView requestView(AdministeredAllocationRequestId requestId, AdministeredAllocationStatus status) {
        return new AdministeredAllocationView(
                requestId.getValue(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                status.name(),
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 5),
                "test",
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }

    @Test
    void approve_request_delegates_to_allocation_port() {
        AdministeredAllocationRequestId requestId = AdministeredAllocationRequestId.of(UUID.randomUUID());
        when(allocationPort.getRequest(requestId)).thenReturn(Optional.of(requestView(requestId, AdministeredAllocationStatus.SUBMITTED)));

        appService.approveRequest(requestId);

        verify(allocationPort).approveRequest(requestId);
    }

    @Test
    void list_requests_delegates_to_allocation_port() {
        appService.listRequests(AdministeredAllocationStatus.SUBMITTED);

        verify(allocationPort).listRequests(AdministeredAllocationStatus.SUBMITTED);
    }

    @Test
    void detect_overdue_delegates_to_allocation_port() {
        appService.detectOverdueAllocations();

        verify(allocationPort).detectOverdueAllocations();
    }

    @Test
    void approve_rejects_non_submitted_request_before_calling_allocation() {
        AdministeredAllocationRequestId requestId = AdministeredAllocationRequestId.of(UUID.randomUUID());
        when(allocationPort.getRequest(requestId)).thenReturn(Optional.of(requestView(requestId, AdministeredAllocationStatus.ACTIVE)));

        assertThatThrownBy(() -> appService.approveRequest(requestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires status SUBMITTED");
    }
}
