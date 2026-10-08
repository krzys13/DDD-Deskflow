package com.deskflow.common.infrastructure.in.web;

import com.deskflow.employees.infrastructure.out.persistence.AllocationLimitEntity;
import com.deskflow.employees.infrastructure.out.persistence.EmployeeEntity;
import com.deskflow.employees.infrastructure.out.persistence.SpringDataEmployeeRepository;
import com.deskflow.identity.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InputValidationSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private SpringDataEmployeeRepository employeeRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String employeeToken;
    private String managerToken;

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

        employeeToken = tokenProvider.issueAccessToken("test.employee@company.test", "EMPLOYEE");
        managerToken = tokenProvider.issueAccessToken("test.kierownik@firma.pl", "MANAGER");
    }

    @Test
    void submit_request_requires_resource_id() throws Exception {
        String body = "{" +
                "\"dateFrom\":\"2099-01-01\"," +
                "\"dateTo\":\"2099-01-02\"," +
                "\"justification\":\"test\"" +
                "}";

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_request_requires_date_from() throws Exception {
        String body = "{" +
                "\"resourceId\":\"" + UUID.randomUUID() + "\"," +
                "\"dateTo\":\"2099-01-02\"," +
                "\"justification\":\"test\"" +
                "}";

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_request_rejects_past_date_from() throws Exception {
        String body = "{" +
                "\"resourceId\":\"" + UUID.randomUUID() + "\"," +
                "\"dateFrom\":\"2000-01-01\"," +
                "\"dateTo\":\"2099-01-02\"," +
                "\"justification\":\"test\"" +
                "}";

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submit_request_requires_date_to() throws Exception {
        String body = "{" +
                "\"resourceId\":\"" + UUID.randomUUID() + "\"," +
                "\"dateFrom\":\"2099-01-01\"," +
                "\"justification\":\"test\"" +
                "}";

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_resource_requires_type() throws Exception {
        String body = "{" +
                "\"name\":\"Desk 1\"," +
                "\"building\":\"A\"," +
                "\"floor\":1," +
                "\"number\":\"101\"" +
                "}";

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_resource_requires_name() throws Exception {
        String body = "{" +
                "\"type\":\"DESK\"," +
                "\"name\":\"\"," +
                "\"building\":\"A\"," +
                "\"floor\":1," +
                "\"number\":\"101\"" +
                "}";

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reject_request_requires_reason() throws Exception {
        String body = "{" +
                "\"reason\":\"\"" +
                "}";

        mockMvc.perform(put("/api/administration/requests/" + UUID.randomUUID() + "/reject")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
