package com.deskflow.common.infrastructure.in.web;

import com.deskflow.administration.application.domain.model.AdministeredAllocationRequestId;
import com.deskflow.administration.application.domain.model.AdministeredAllocationStatus;
import com.deskflow.administration.application.model.AdministeredAllocationView;
import com.deskflow.administration.application.port.in.AdministrationInputPort;
import com.deskflow.administration.infrastructure.in.web.AdministrationController;
import com.deskflow.allocation.application.model.ResourceCatalogItem;
import com.deskflow.allocation.application.port.in.AllocationRequestInputPort;
import com.deskflow.allocation.infrastructure.in.web.AllocationRequestController;
import com.deskflow.allocation.infrastructure.in.web.CurrentUserResolver;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.employees.application.domain.model.vo.Role;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.employees.infrastructure.in.web.EmployeeAdminController;
import com.deskflow.identity.application.model.AuthenticatedIdentity;
import com.deskflow.identity.application.model.IdentityProfile;
import com.deskflow.identity.application.port.in.IdentityAccessInputPort;
import com.deskflow.identity.infrastructure.in.web.AuthController;
import com.deskflow.identity.infrastructure.security.JwtAuthenticationFilter;
import com.deskflow.identity.infrastructure.security.JwtTokenProvider;
import com.deskflow.identity.infrastructure.security.SecurityConfig;
import com.deskflow.resources.application.port.in.ResourceManagementInputPort;
import com.deskflow.resources.infrastructure.in.web.ResourceController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AllocationRequestController.class,
        AdministrationController.class,
        EmployeeAdminController.class,
        AuthController.class,
        ResourceController.class
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class RestControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private JwtTokenProvider tokenProvider;
    @MockitoBean private AllocationRequestInputPort allocationRequestInputPort;
    @MockitoBean private AdministrationInputPort administrationInputPort;
    @MockitoBean private EmployeeManagementInputPort employeeManagementInputPort;
    @MockitoBean private IdentityAccessInputPort identityAccessInputPort;
    @MockitoBean private ResourceManagementInputPort resourceManagementInputPort;
    @MockitoBean private CurrentUserResolver currentUserResolver;

    private final UUID employeeUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID resourceUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID requestUuid = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @BeforeEach
    void setUpTokens() {
        token("employee-token", "employee@test.pl", "EMPLOYEE");
        token("manager-token", "manager@test.pl", "MANAGER");
        token("admin-token", "admin@test.pl", "IT_ADMIN");
        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);
    }

    @Test
    void auth_login_returns_identity_with_token() throws Exception {
        when(identityAccessInputPort.login("user@test.pl", "secret"))
                .thenReturn(new AuthenticatedIdentity("jwt", "user@test.pl", "MANAGER", "Jan", "Kowalski", employeeUuid.toString()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.pl\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.email").value("user@test.pl"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void auth_me_returns_current_identity_for_valid_token() throws Exception {
        when(identityAccessInputPort.currentIdentity("employee@test.pl"))
                .thenReturn(Optional.of(new IdentityProfile("employee@test.pl", "EMPLOYEE", "Anna", "Nowak", employeeUuid.toString())));

        mockMvc.perform(get("/api/auth/me").with(authenticated("employee-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("employee@test.pl"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void auth_me_returns_not_found_when_profile_is_missing() throws Exception {
        when(identityAccessInputPort.currentIdentity("employee@test.pl")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me").with(authenticated("employee-token")))
                .andExpect(status().isNotFound());
    }

    @Test
    void resource_browsing_requires_authentication() throws Exception {
        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void resource_browsing_returns_catalog_for_authenticated_user() throws Exception {
        when(allocationRequestInputPort.browseResources()).thenReturn(List.of(
                new ResourceCatalogItem(resourceUuid, "DESK", "Desk 1", "A", 1, "101", true)
        ));

        mockMvc.perform(get("/api/resources").with(authenticated("employee-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(resourceUuid.toString()))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void employee_cannot_add_resource() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .with(authenticated("employee-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourceBody("DESK", "Desk 1", "A", "101")))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_can_add_resource() throws Exception {
        when(resourceManagementInputPort.addResource(ResourceType.DESK, "Desk 1", "A", 1, "101"))
                .thenReturn(ResourceId.of(resourceUuid));

        mockMvc.perform(post("/api/resources")
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourceBody("DESK", "Desk 1", "A", "101")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceId").value(resourceUuid.toString()));

        verify(resourceManagementInputPort).addResource(ResourceType.DESK, "Desk 1", "A", 1, "101");
    }

    @Test
    void add_resource_rejects_blank_building_and_number() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourceBody("DESK", "Desk 1", "", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_resource_rejects_invalid_type() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourceBody("INVALID", "Desk 1", "A", "101")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_resource_rejects_malformed_json() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void add_resource_rejects_missing_content_type() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .with(authenticated("manager-token"))
                        .content(resourceBody("DESK", "Desk 1", "A", "101")))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void manager_can_update_resource_availability() throws Exception {
        mockMvc.perform(put("/api/resources/" + resourceUuid + "/unavailable")
                        .with(authenticated("manager-token")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/resources/" + resourceUuid + "/available")
                        .with(authenticated("manager-token")))
                .andExpect(status().isOk());

        verify(resourceManagementInputPort).markUnavailable(ResourceId.of(resourceUuid));
        verify(resourceManagementInputPort).markAvailable(ResourceId.of(resourceUuid));
    }

    @Test
    void resource_path_rejects_invalid_uuid() throws Exception {
        mockMvc.perform(put("/api/resources/not-a-uuid/unavailable")
                        .with(authenticated("manager-token")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void only_it_admin_can_delete_resource() throws Exception {
        mockMvc.perform(delete("/api/resources/" + resourceUuid)
                        .with(authenticated("manager-token")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/resources/" + resourceUuid)
                        .with(authenticated("admin-token")))
                .andExpect(status().isOk());

        verify(resourceManagementInputPort).deactivateResource(ResourceId.of(resourceUuid));
    }

    @Test
    void employee_admin_lists_employees_with_limits() throws Exception {
        Employee employee = new Employee(EmployeeId.of(employeeUuid), "employee@test.pl", "Anna", "Nowak", Role.EMPLOYEE,
                Map.of(ResourceType.DESK, 2));
        when(employeeManagementInputPort.listEmployees()).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/administration/employees").with(authenticated("manager-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(employeeUuid.toString()))
                .andExpect(jsonPath("$[0].limits.DESK").value(2));
    }

    @Test
    void employee_admin_rejects_invalid_limit_update() throws Exception {
        mockMvc.perform(put("/api/administration/employees/" + employeeUuid + "/limits")
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceType\":\"DESK\",\"amount\":-1}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/administration/employees/" + employeeUuid + "/limits")
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceType\":\"INVALID\",\"amount\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void employee_admin_updates_limit_and_later_lists_updated_employee() throws Exception {
        mockMvc.perform(put("/api/administration/employees/" + employeeUuid + "/limits")
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceType\":\"DESK\",\"amount\":5}"))
                .andExpect(status().isOk());

        verify(employeeManagementInputPort).upsertLimit(EmployeeId.of(employeeUuid), "DESK", 5);

        Employee updatedEmployee = new Employee(EmployeeId.of(employeeUuid), "employee@test.pl", "Anna", "Nowak", Role.EMPLOYEE,
                Map.of(ResourceType.DESK, 5));
        when(employeeManagementInputPort.listEmployees()).thenReturn(List.of(updatedEmployee));

        mockMvc.perform(get("/api/administration/employees").with(authenticated("manager-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(employeeUuid.toString()))
                .andExpect(jsonPath("$[0].limits.DESK").value(5));
    }

    @Test
    void administration_lists_requests_with_status_filter_and_employee_names() throws Exception {
        when(administrationInputPort.listRequests(AdministeredAllocationStatus.SUBMITTED))
                .thenReturn(List.of(adminView("SUBMITTED")));
        when(employeeManagementInputPort.getEmployee(EmployeeId.of(employeeUuid)))
                .thenReturn(Optional.of(new Employee(EmployeeId.of(employeeUuid), "employee@test.pl", "Anna", "Nowak", Role.EMPLOYEE, Map.of())));

        mockMvc.perform(get("/api/administration/requests?status=SUBMITTED")
                .with(authenticated("manager-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestUuid.toString()))
                .andExpect(jsonPath("$[0].employeeFirstName").value("Anna"));

        verify(administrationInputPort).listRequests(AdministeredAllocationStatus.SUBMITTED);
    }

    @Test
    void administration_rejects_invalid_status_filter() throws Exception {
        mockMvc.perform(get("/api/administration/requests?status=BAD_STATUS")
                        .with(authenticated("manager-token")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void administration_returns_conflict_for_invalid_lifecycle_operation() throws Exception {
        doThrow(new IllegalStateException("Request requires status SUBMITTED"))
                .when(administrationInputPort).approveRequest(AdministeredAllocationRequestId.of(requestUuid));

        mockMvc.perform(put("/api/administration/requests/" + requestUuid + "/approve")
                        .with(authenticated("manager-token")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Request requires status SUBMITTED"));
    }

    @Test
    void submit_request_rejects_date_to_before_date_from() throws Exception {
        when(currentUserResolver.resolveEmployeeId(any())).thenReturn(EmployeeId.of(employeeUuid));
        doThrow(new IllegalArgumentException("dateTo must not be before dateFrom"))
                .when(allocationRequestInputPort).submitRequest(
                        eq(EmployeeId.of(employeeUuid)), eq(ResourceId.of(resourceUuid)),
                        eq(LocalDate.of(2099, 1, 10)), eq(LocalDate.of(2099, 1, 1)), eq("test"));

        mockMvc.perform(post("/api/requests")
                        .with(authenticated("employee-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceId\":\"" + resourceUuid + "\",\"dateFrom\":\"2099-01-10\",\"dateTo\":\"2099-01-01\",\"justification\":\"test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("dateTo must not be before dateFrom"));
    }

    @Test
    void missing_resource_from_controller_returns_not_found() throws Exception {
        doThrow(new NoSuchElementException("Resource not found"))
                .when(resourceManagementInputPort).updateResource(ResourceId.of(resourceUuid), "Desk 1", "A", 1, "101");

        mockMvc.perform(put("/api/resources/" + resourceUuid)
                        .with(authenticated("manager-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourceBody("DESK", "Desk 1", "A", "101")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource not found"));
    }

    private void token(String token, String email, String role) {
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(tokenProvider.getRoleFromToken(token)).thenReturn(role);
    }

    private RequestPostProcessor authenticated(String token) {
        return switch (token) {
            case "employee-token" -> user("employee@test.pl").roles("EMPLOYEE");
            case "manager-token" -> user("manager@test.pl").roles("MANAGER");
            case "admin-token" -> user("admin@test.pl").roles("IT_ADMIN");
            default -> user("unknown@test.pl");
        };
    }

    private String resourceBody(String type, String name, String building, String number) {
        return "{\"type\":\"" + type + "\",\"name\":\"" + name + "\",\"building\":\"" + building +
                "\",\"floor\":1,\"number\":\"" + number + "\"}";
    }

    private AdministeredAllocationView adminView(String status) {
        return new AdministeredAllocationView(
                requestUuid,
                employeeUuid,
                resourceUuid,
                status,
                LocalDate.of(2099, 1, 1),
                LocalDate.of(2099, 1, 2),
                "test",
                LocalDateTime.of(2098, 12, 1, 10, 0),
                null,
                null,
                null
        );
    }
}
