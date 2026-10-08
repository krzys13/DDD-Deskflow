package com.deskflow.resources.infrastructure.out.allocation;

import com.deskflow.allocation.application.model.AllocationRequestView;
import com.deskflow.allocation.application.port.in.AllocationRequestInputPort;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.resources.application.domain.model.vo.AllocationUsageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationRequestAdapterTest {

    @Mock private AllocationRequestInputPort allocationRequestInputPort;

    private AllocationRequestAdapter adapter;
    private ResourceId resourceId;

    @BeforeEach
    void setUp() {
        adapter = new AllocationRequestAdapter(allocationRequestInputPort);
        resourceId = ResourceId.of(UUID.randomUUID());
    }

    private AllocationRequestView viewWithStatus(String status) {
        return new AllocationRequestView(UUID.randomUUID(), UUID.randomUUID(), resourceId.getValue(),
                status, LocalDate.now(), LocalDate.now().plusDays(1), "test",
                LocalDateTime.now(), null, null, null);
    }

    @Test
    void submitted_maps_to_requested() {
        when(allocationRequestInputPort.findActiveForResource(resourceId))
                .thenReturn(List.of(viewWithStatus("SUBMITTED")));

        var statuses = adapter.findStatusesForResource(resourceId);

        assertThat(statuses).containsExactly(AllocationUsageStatus.REQUESTED);
    }

    @Test
    void approved_maps_to_approved() {
        when(allocationRequestInputPort.findActiveForResource(resourceId))
                .thenReturn(List.of(viewWithStatus("APPROVED")));

        var statuses = adapter.findStatusesForResource(resourceId);

        assertThat(statuses).containsExactly(AllocationUsageStatus.APPROVED);
    }

    @Test
    void active_maps_to_in_use() {
        when(allocationRequestInputPort.findActiveForResource(resourceId))
                .thenReturn(List.of(viewWithStatus("ACTIVE")));

        var statuses = adapter.findStatusesForResource(resourceId);

        assertThat(statuses).containsExactly(AllocationUsageStatus.IN_USE);
    }

    @Test
    void overdue_maps_to_overdue() {
        when(allocationRequestInputPort.findActiveForResource(resourceId))
                .thenReturn(List.of(viewWithStatus("OVERDUE")));

        var statuses = adapter.findStatusesForResource(resourceId);

        assertThat(statuses).containsExactly(AllocationUsageStatus.OVERDUE);
    }

    @Test
    void rejected_cancelled_completed_map_to_closed() {
        when(allocationRequestInputPort.findActiveForResource(resourceId))
                .thenReturn(List.of(
                        viewWithStatus("REJECTED"),
                        viewWithStatus("CANCELLED"),
                        viewWithStatus("COMPLETED")
                ));

        var statuses = adapter.findStatusesForResource(resourceId);

        assertThat(statuses).allMatch(s -> s == AllocationUsageStatus.CLOSED);
    }

    @Test
    void unknown_status_throws() {
        when(allocationRequestInputPort.findActiveForResource(resourceId))
                .thenReturn(List.of(viewWithStatus("UNKNOWN")));

        assertThatThrownBy(() -> adapter.findStatusesForResource(resourceId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown allocation status");
    }

    @Test
    void returns_multiple_statuses() {
        when(allocationRequestInputPort.findActiveForResource(resourceId))
                .thenReturn(List.of(
                        viewWithStatus("APPROVED"),
                        viewWithStatus("ACTIVE")
                ));

        var statuses = adapter.findStatusesForResource(resourceId);

        assertThat(statuses).containsExactly(AllocationUsageStatus.APPROVED, AllocationUsageStatus.IN_USE);
    }
}
