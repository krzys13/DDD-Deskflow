package com.deskflow.allocation.infrastructure.out.resources;

import com.deskflow.allocation.application.domain.model.ResourceAllocationProfile;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.resources.application.model.ResourceView;
import com.deskflow.resources.application.port.in.ResourceManagementInputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceCatalogAdapterTest {

    @Mock private ResourceManagementInputPort resourceManagementInputPort;

    private ResourceCatalogAdapter adapter;
    private ResourceId resourceId;

    @BeforeEach
    void setUp() {
        adapter = new ResourceCatalogAdapter(resourceManagementInputPort);
        resourceId = ResourceId.of(UUID.randomUUID());
    }

    @Test
    void findResource_returns_profile_when_found() {
        var view = new ResourceView(resourceId.getValue(), "DESK", "Biurko A", "A", 1, "101", true, true);
        when(resourceManagementInputPort.getResource(resourceId)).thenReturn(Optional.of(view));

        var result = adapter.findResource(resourceId);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(resourceId);
        assertThat(result.get().type()).isEqualTo("DESK");
        assertThat(result.get().name()).isEqualTo("Biurko A");
        assertThat(result.get().building()).isEqualTo("A");
        assertThat(result.get().floor()).isEqualTo(1);
        assertThat(result.get().number()).isEqualTo("101");
        assertThat(result.get().active()).isTrue();
        assertThat(result.get().available()).isTrue();
    }

    @Test
    void findResource_returns_empty_when_not_found() {
        when(resourceManagementInputPort.getResource(resourceId)).thenReturn(Optional.empty());

        var result = adapter.findResource(resourceId);

        assertThat(result).isEmpty();
    }

    @Test
    void findActiveResources_returns_all_active() {
        var view1 = new ResourceView(UUID.randomUUID(), "DESK", "Biurko A", "A", 1, "101", true, true);
        var view2 = new ResourceView(UUID.randomUUID(), "LAPTOP", "Laptop X", "B", 2, "201", true, false);
        when(resourceManagementInputPort.getActiveResources()).thenReturn(List.of(view1, view2));

        var profiles = adapter.findActiveResources();

        assertThat(profiles).hasSize(2);
        assertThat(profiles).extracting(ResourceAllocationProfile::active).containsOnly(true);
    }

    @Test
    void findActiveResources_returns_empty_when_none() {
        when(resourceManagementInputPort.getActiveResources()).thenReturn(List.of());

        var profiles = adapter.findActiveResources();

        assertThat(profiles).isEmpty();
    }
}
