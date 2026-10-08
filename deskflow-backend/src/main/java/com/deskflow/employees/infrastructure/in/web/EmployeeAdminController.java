package com.deskflow.employees.infrastructure.in.web;

import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import com.deskflow.employees.application.domain.model.Employee;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/administration/employees")
public class EmployeeAdminController {

    private final EmployeeManagementInputPort employeeManagementInputPort;

    public EmployeeAdminController(EmployeeManagementInputPort employeeManagementInputPort) {
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listEmployees() {
        List<Map<String, Object>> result = employeeManagementInputPort.listEmployees().stream()
                .map(e -> Map.<String, Object>of(
                        "id", e.getId().getValue(),
                        "email", e.getEmail(),
                        "firstName", e.getFirstName(),
                        "lastName", e.getLastName(),
                        "role", e.getRole().name(),
                        "limits", buildLimits(e)
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/limits")
    public ResponseEntity<Void> upsertLimit(@PathVariable UUID id, @Valid @RequestBody LimitRequest body) {
        ResourceType.valueOf(body.resourceType());
        employeeManagementInputPort.upsertLimit(EmployeeId.of(id), body.resourceType(), body.amount());
        return ResponseEntity.ok().build();
    }

    private Map<String, Integer> buildLimits(Employee e) {
        return Arrays.stream(ResourceType.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        t -> e.limitForType(t)
                ));
    }

    public record LimitRequest(
            @NotBlank String resourceType,
            @Min(0) int amount
    ) {}
}
