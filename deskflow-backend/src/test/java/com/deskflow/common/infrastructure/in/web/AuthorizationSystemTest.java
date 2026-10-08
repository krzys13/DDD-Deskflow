package com.deskflow.common.infrastructure.in.web;

import com.deskflow.identity.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationSystemTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String employeeToken;
    private String managerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        employeeToken = tokenProvider.issueAccessToken("test.employee@company.test", "EMPLOYEE");
        managerToken = tokenProvider.issueAccessToken("test.manager@company.test", "MANAGER");
        adminToken = tokenProvider.issueAccessToken("test.admin@company.test", "IT_ADMIN");
    }

    @Test
    void admin_endpoint_requires_authentication() throws Exception {
        mockMvc.perform(get("/api/administration/requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalid_token_is_unauthorized() throws Exception {
        mockMvc.perform(get("/api/administration/requests")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employee_role_cannot_access_admin_endpoints() throws Exception {
        mockMvc.perform(get("/api/administration/requests")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_role_can_access_admin_endpoints() throws Exception {
        mockMvc.perform(get("/api/administration/requests")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
    }

    @Test
    void resources_browse_requires_authentication() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void auth_endpoints_are_public_but_still_validate_input() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void manager_role_cannot_delete_resources() throws Exception {
        mockMvc.perform(delete("/api/resources/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_role_is_authorized_to_delete_resources() throws Exception {
        mockMvc.perform(delete("/api/resources/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
