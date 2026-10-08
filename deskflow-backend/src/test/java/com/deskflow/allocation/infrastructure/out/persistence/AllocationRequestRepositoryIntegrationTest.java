package com.deskflow.allocation.infrastructure.out.persistence;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        com.deskflow.allocation.infrastructure.out.persistence.HibernateAllocationRequestRepositoryAdapter.class,
        com.deskflow.allocation.infrastructure.out.persistence.AllocationRequestMapper.class
})
class AllocationRequestRepositoryIntegrationTest {

    @Autowired
    private AllocationRequestRepositoryPort requestRepository;

    private final AllocationRequestFactory factory = new AllocationRequestFactory();
    private final EmployeeId employeeId = EmployeeId.of(UUID.randomUUID());
    private final ResourceId resourceId = ResourceId.of(UUID.randomUUID());

    @Test
    void save_and_find_request() {
        DateRange dateRange = new DateRange(LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 5));
        AllocationRequest request = factory.create(employeeId, resourceId, dateRange, "test");

        requestRepository.save(request);

        Optional<AllocationRequest> found = requestRepository.find(request.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(RequestStatus.SUBMITTED);
        assertThat(found.get().getEmployeeId()).isEqualTo(employeeId);
    }

    @Test
    void find_overdue_returns_active_past_due_requests() {
        DateRange pastRange = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10));
        AllocationRequest request = AllocationRequest.reconstitute(
                AllocationRequestId.newId(),
                employeeId, resourceId, pastRange, "test",
                LocalDateTime.of(2024, 1, 1, 9, 0),
                RequestStatus.ACTIVE, LocalDateTime.of(2024, 1, 1, 10, 0), null, null
        );
        requestRepository.save(request);

        List<AllocationRequest> overdue = requestRepository.findOverdue(LocalDateTime.now());

        assertThat(overdue).anyMatch(r -> r.getId().equals(request.getId()));
    }

    @Test
    void find_active_for_resource_with_overlapping_date_range() {
        DateRange dateRange = new DateRange(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 10));
        AllocationRequest request = factory.create(employeeId, resourceId, dateRange, "test");
        requestRepository.save(request);

        DateRange searchRange = new DateRange(LocalDate.of(2025, 8, 5), LocalDate.of(2025, 8, 15));
        List<AllocationRequest> active = requestRepository.findActiveForResource(resourceId, searchRange);

        assertThat(active).anyMatch(r -> r.getId().equals(request.getId()));
    }
}
