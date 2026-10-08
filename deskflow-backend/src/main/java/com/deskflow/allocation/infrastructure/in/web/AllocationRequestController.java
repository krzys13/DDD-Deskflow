package com.deskflow.allocation.infrastructure.in.web;

import com.deskflow.allocation.application.port.out.EmployeeProfilePort;
import com.deskflow.allocation.infrastructure.in.web.dto.AllocationRequestDTO;
import com.deskflow.allocation.infrastructure.in.web.dto.AllocationRequestResponseDTO;
import com.deskflow.allocation.application.port.in.AllocationRequestInputPort;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.allocation.application.domain.model.vo.AllocationRequestId;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.employees.application.domain.model.Employee;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AllocationRequestController {

    private final AllocationRequestInputPort allocationRequestInputPort;
    private final CurrentUserResolver userResolver;
    private final EmployeeManagementInputPort employeeManagementInputPort;

    public AllocationRequestController(AllocationRequestInputPort allocationRequestInputPort,
                                       CurrentUserResolver userResolver,
                                       EmployeeManagementInputPort employeeManagementInputPort) {
        this.allocationRequestInputPort = allocationRequestInputPort;
        this.userResolver = userResolver;
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    @GetMapping("/resources")
    public ResponseEntity<?> browseResources() {
        return ResponseEntity.ok(allocationRequestInputPort.browseResources().stream()
                .map(r -> new java.util.LinkedHashMap<>(java.util.Map.of(
                        "id", r.id(),
                        "type", r.type(),
                        "name", r.name(),
                        "building", r.building(),
                        "floor", r.floor(),
                        "number", r.number(),
                        "available", r.available()
                )))
                .toList());
    }

    @PostMapping("/requests")
    public ResponseEntity<?> submitRequest(@Valid @RequestBody AllocationRequestDTO dto, Authentication auth) {
        EmployeeId employeeId = userResolver.resolveEmployeeId(auth);
        AllocationRequestId id = allocationRequestInputPort.submitRequest(
                employeeId, ResourceId.of(dto.resourceId()),
                dto.dateFrom(), dto.dateTo(), dto.justification());
        return ResponseEntity.created(URI.create("/api/requests/" + id))
                .body(java.util.Map.of("requestId", id.getValue()));
    }

    @PutMapping("/requests/{id}/cancel")
    public ResponseEntity<Void> cancelRequest(@PathVariable UUID id, Authentication auth) {
        EmployeeId employeeId = userResolver.resolveEmployeeId(auth);
        allocationRequestInputPort.cancelRequest(AllocationRequestId.of(id), employeeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests/my")
    public ResponseEntity<List<AllocationRequestResponseDTO>> browseHistory(Authentication auth) {
        EmployeeId employeeId = userResolver.resolveEmployeeId(auth);
        Employee employee = employeeManagementInputPort.getEmployee(employeeId).orElse(null);
        List<AllocationRequestResponseDTO> history = allocationRequestInputPort.browseHistory(employeeId)
                .stream()
                .map(r -> AllocationRequestRestMapper.toResponse(r,
                        employee != null ? employee.getFirstName() : null,
                        employee != null ? employee.getLastName() : null))
                .toList();
        return ResponseEntity.ok(history);
    }
}
