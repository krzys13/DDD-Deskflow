package com.deskflow.administration.infrastructure.out.allocation;

import com.deskflow.administration.application.model.AdministeredAllocationView;
import com.deskflow.administration.application.domain.model.AdministeredAllocationRequestId;
import com.deskflow.administration.application.domain.model.AdministeredAllocationStatus;
import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.application.port.in.AllocationAdministrationInputPort;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationAdministrationAdapterTest {

    @Mock private AllocationAdministrationInputPort allocationAdministrationInputPort;

    private AllocationAdministrationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AllocationAdministrationAdapter(allocationAdministrationInputPort);
    }

    @Test
    void getRequest_returns_admin_view_when_found() {
        var adminId = AdministeredAllocationRequestId.of(UUID.randomUUID());
        var allocView = new AllocationRequestView(adminId.getValue(), UUID.randomUUID(), UUID.randomUUID(),
                "SUBMITTED", LocalDate.now(), LocalDate.now().plusDays(1), "test",
                LocalDateTime.now(), null, null, null);
        when(allocationAdministrationInputPort.getRequest(AllocationRequestId.of(adminId.getValue())))
                .thenReturn(Optional.of(allocView));

        var result = adapter.getRequest(adminId);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(adminId.getValue());
        assertThat(result.get().status()).isEqualTo("SUBMITTED");
    }

    @Test
    void getRequest_returns_empty_when_not_found() {
        var adminId = AdministeredAllocationRequestId.of(UUID.randomUUID());
        when(allocationAdministrationInputPort.getRequest(any())).thenReturn(Optional.empty());

        var result = adapter.getRequest(adminId);

        assertThat(result).isEmpty();
    }

    @Test
    void listRequests_converts_status_and_maps() {
        when(allocationAdministrationInputPort.listRequests(any()))
                .thenReturn(List.of(
                        new AllocationRequestView(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                                "SUBMITTED", LocalDate.now(), LocalDate.now().plusDays(1), "test",
                                LocalDateTime.now(), null, null, null)
                ));

        var result = adapter.listRequests(AdministeredAllocationStatus.SUBMITTED);

        assertThat(result).hasSize(1);
        verify(allocationAdministrationInputPort).listRequests(com.deskflow.allocation.application.domain.model.vo.RequestStatus.SUBMITTED);
    }

    @Test
    void listRequests_passes_null_status() {
        when(allocationAdministrationInputPort.listRequests(null)).thenReturn(List.of());

        var result = adapter.listRequests(null);

        assertThat(result).isEmpty();
    }

    @Test
    void approveRequest_delegates() {
        var adminId = AdministeredAllocationRequestId.of(UUID.randomUUID());

        adapter.approveRequest(adminId);

        verify(allocationAdministrationInputPort).approveRequest(AllocationRequestId.of(adminId.getValue()));
    }

    @Test
    void rejectRequest_delegates() {
        var adminId = AdministeredAllocationRequestId.of(UUID.randomUUID());

        adapter.rejectRequest(adminId, "No space");

        verify(allocationAdministrationInputPort).rejectRequest(AllocationRequestId.of(adminId.getValue()), "No space");
    }

    @Test
    void issueResource_delegates() {
        var adminId = AdministeredAllocationRequestId.of(UUID.randomUUID());

        adapter.issueResource(adminId);

        verify(allocationAdministrationInputPort).issueResource(AllocationRequestId.of(adminId.getValue()));
    }

    @Test
    void returnResource_delegates() {
        var adminId = AdministeredAllocationRequestId.of(UUID.randomUUID());

        adapter.returnResource(adminId);

        verify(allocationAdministrationInputPort).returnResource(AllocationRequestId.of(adminId.getValue()));
    }

    @Test
    void monitorActiveAllocations_returns_mapped_views() {
        when(allocationAdministrationInputPort.monitorActiveAllocations())
                .thenReturn(List.of(
                        new AllocationRequestView(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                                "ACTIVE", LocalDate.now(), LocalDate.now().plusDays(1), "test",
                                LocalDateTime.now(), LocalDateTime.now(), null, null)
                ));

        var result = adapter.monitorActiveAllocations();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("ACTIVE");
    }

    @Test
    void detectOverdueAllocations_delegates() {
        adapter.detectOverdueAllocations();

        verify(allocationAdministrationInputPort).detectOverdueAllocations();
    }
}
