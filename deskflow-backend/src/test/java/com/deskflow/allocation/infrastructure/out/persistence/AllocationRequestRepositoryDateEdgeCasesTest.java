package com.deskflow.allocation.infrastructure.out.persistence;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        com.deskflow.allocation.infrastructure.out.persistence.HibernateAllocationRequestRepositoryAdapter.class,
        com.deskflow.allocation.infrastructure.out.persistence.AllocationRequestMapper.class
})
class AllocationRequestRepositoryDateEdgeCasesIntegrationTest {

    @Autowired
    private AllocationRequestRepositoryPort requestRepository;

    private final AllocationRequestFactory factory = new AllocationRequestFactory();
    private final EmployeeId employeeId = EmployeeId.of(UUID.randomUUID());
    private final ResourceId resourceId = ResourceId.of(UUID.randomUUID());

    @Test
    void overlapping_when_search_starts_on_request_end_is_included() {
        DateRange requestRange = new DateRange(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 10));
        AllocationRequest request = factory.create(employeeId, resourceId, requestRange, "test");
        requestRepository.save(request);

        DateRange searchRange = new DateRange(LocalDate.of(2025, 8, 10), LocalDate.of(2025, 8, 12));
        List<AllocationRequest> results = requestRepository.findActiveForResource(resourceId, searchRange);

        assertThat(results).anyMatch(r -> r.getId().equals(request.getId()));
    }

    @Test
    void no_overlap_when_request_ends_before_search_starts_is_not_included() {
        DateRange requestRange = new DateRange(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 10));
        AllocationRequest request = factory.create(employeeId, resourceId, requestRange, "test");
        requestRepository.save(request);

        DateRange searchRange = new DateRange(LocalDate.of(2025, 8, 11), LocalDate.of(2025, 8, 12));
        List<AllocationRequest> results = requestRepository.findActiveForResource(resourceId, searchRange);

        assertThat(results).noneMatch(r -> r.getId().equals(request.getId()));
    }

    @Test
    void overlapping_when_request_inside_search_is_included() {
        DateRange requestRange = new DateRange(LocalDate.of(2025, 8, 5), LocalDate.of(2025, 8, 6));
        AllocationRequest request = factory.create(employeeId, resourceId, requestRange, "test");
        requestRepository.save(request);

        DateRange searchRange = new DateRange(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 10));
        List<AllocationRequest> results = requestRepository.findActiveForResource(resourceId, searchRange);

        assertThat(results).anyMatch(r -> r.getId().equals(request.getId()));
    }
}
