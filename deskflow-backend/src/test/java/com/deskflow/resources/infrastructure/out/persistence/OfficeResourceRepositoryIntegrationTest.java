package com.deskflow.resources.infrastructure.out.persistence;

import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.port.out.OfficeResourceRepositoryPort;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        com.deskflow.resources.infrastructure.out.persistence.HibernateOfficeResourceRepositoryAdapter.class,
        com.deskflow.resources.infrastructure.out.persistence.OfficeResourceMapper.class
})
class OfficeResourceRepositoryIntegrationTest {

    @Autowired
    private OfficeResourceRepositoryPort resourceRepository;

    private final OfficeResourceFactory factory = new OfficeResourceFactory();

    @Test
    void save_and_find_resource() {
        OfficeResource resource = factory.create(ResourceType.DESK, "Desk A1", new Location("A", 1, "101"));

        resourceRepository.save(resource);

        ResourceId id = resource.getId();
        OfficeResource found = resourceRepository.find(id).orElseThrow();
        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getType()).isEqualTo(ResourceType.DESK);
        assertThat(found.getLocation()).isEqualTo(resource.getLocation());
    }

    @Test
    void find_active_returns_only_active_resources() {
        OfficeResource active = factory.create(ResourceType.DESK, "Desk A2", new Location("A", 1, "102"));
        OfficeResource inactive = factory.create(ResourceType.DESK, "Desk A3", new Location("A", 1, "103"));
        inactive.deactivate();

        resourceRepository.save(active);
        resourceRepository.save(inactive);

        List<OfficeResource> activeResources = resourceRepository.findActive();

        assertThat(activeResources).anyMatch(r -> r.getId().equals(active.getId()));
        assertThat(activeResources).noneMatch(r -> r.getId().equals(inactive.getId()));
    }

    @Test
    void find_active_returns_empty_when_no_active_resources() {
        OfficeResource inactive = factory.create(ResourceType.DESK, "Desk A4", new Location("A", 1, "104"));
        inactive.deactivate();
        resourceRepository.save(inactive);

        List<OfficeResource> activeResources = resourceRepository.findActive();

        assertThat(activeResources).isEmpty();
    }

    @Test
    void find_returns_empty_when_resource_missing() {
        var missingId = ResourceId.of(UUID.randomUUID());

        assertThat(resourceRepository.find(missingId)).isEmpty();
    }
}
