package com.deskflow.common.infrastructure.in.web;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.AllocationRequest;
import com.deskflow.allocation.application.domain.service.AllocationRequestFactory;
import com.deskflow.allocation.application.port.out.AllocationRequestRepositoryPort;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.allocation.application.domain.model.vo.DateRange;
import com.deskflow.allocation.application.domain.model.vo.RequestStatus;
import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.resources.application.domain.service.OfficeResourceFactory;
import com.deskflow.resources.application.port.out.OfficeResourceRepositoryPort;
import com.deskflow.resources.application.domain.model.vo.Location;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.employees.infrastructure.out.persistence.AllocationLimitEntity;
import com.deskflow.employees.infrastructure.out.persistence.EmployeeEntity;
import com.deskflow.employees.infrastructure.out.persistence.SpringDataEmployeeRepository;
import com.deskflow.identity.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FullAllocationCycleSystemTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenProvider tokenProvider;
    @Autowired AllocationRequestRepositoryPort requestRepository;
    @Autowired OfficeResourceRepositoryPort resourceRepository;
    @Autowired SpringDataEmployeeRepository employeeRepo;
    @Autowired PasswordEncoder passwordEncoder;

    private String employeeToken;
    private String managerToken;
    private EmployeeId employeeId;
    private UUID resourceUuid;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setId(UUID.randomUUID());
        employee.setEmail("test.employee@company.test");
        employee.setFirstName("Test");
        employee.setLastName("Employee");
        employee.setRole("EMPLOYEE");
        employee.setPasswordHash(passwordEncoder.encode("pass"));
        AllocationLimitEntity limit = new AllocationLimitEntity();
        limit.setId(UUID.randomUUID());
        limit.setResourceType("DESK");
        limit.setLimitAmount(3);
        employee.addAllocationLimit(limit);
        employeeRepo.save(employee);
        employeeId = EmployeeId.of(employee.getId());

        EmployeeEntity manager = new EmployeeEntity();
        manager.setId(UUID.randomUUID());
        manager.setEmail("test.kierownik@firma.pl");
        manager.setFirstName("Test");
        manager.setLastName("Kierownik");
        manager.setRole("MANAGER");
        manager.setPasswordHash(passwordEncoder.encode("pass"));
        employeeRepo.save(manager);

        employeeToken = tokenProvider.issueAccessToken("test.employee@company.test", "EMPLOYEE");
        managerToken = tokenProvider.issueAccessToken("test.kierownik@firma.pl", "MANAGER");

        OfficeResource resource = new OfficeResourceFactory().create(
                ResourceType.DESK, "Biurko testowe", new Location("A", 1, "T01")
        );
        resourceRepository.save(resource);
        resourceUuid = resource.getId().getValue();
    }

    @Test
    void full_cycle_submit_approve_issue_return() throws Exception {
        // UC-02: Submit request
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "resourceId", resourceUuid,
                "dateFrom", LocalDate.now().plusDays(1).toString(),
                "dateTo", LocalDate.now().plusDays(5).toString(),
                "justification", "Test systemowy"
        ));

        String responseBody = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID requestId = UUID.fromString(
                objectMapper.readTree(responseBody).get("requestId").asText()
        );

        assertThat(requestRepository.find(AllocationRequestId.of(requestId)).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.SUBMITTED);

        // UC-05: Approve request
        mockMvc.perform(put("/api/administration/requests/" + requestId + "/approve")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        assertThat(requestRepository.find(AllocationRequestId.of(requestId)).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.APPROVED);

        // UC-07: Issue resource
        mockMvc.perform(put("/api/administration/requests/" + requestId + "/issue")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        assertThat(requestRepository.find(AllocationRequestId.of(requestId)).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.ACTIVE);

        // UC-08: Return resource
        mockMvc.perform(put("/api/administration/requests/" + requestId + "/return")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
        assertThat(requestRepository.find(AllocationRequestId.of(requestId)).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.COMPLETED);
    }

    @Test
    void employee_can_cancel_submitted_request() throws Exception {
        DateRange dateRange = new DateRange(LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
        AllocationRequest request = new AllocationRequestFactory().create(
                employeeId, com.deskflow.common.application.domain.model.vo.ResourceId.of(resourceUuid), dateRange, "test"
        );
        requestRepository.save(request);

        mockMvc.perform(put("/api/requests/" + request.getId().getValue() + "/cancel")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        assertThat(requestRepository.find(request.getId()).orElseThrow().getStatus())
                .isEqualTo(RequestStatus.CANCELLED);
    }
}
