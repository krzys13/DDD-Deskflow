package com.deskflow.administration.infrastructure.in.web;

import com.deskflow.administration.application.model.AdministeredAllocationView;
import com.deskflow.administration.infrastructure.in.web.dto.RejectRequestDTO;
import com.deskflow.allocation.infrastructure.in.web.dto.AllocationRequestResponseDTO;
import com.deskflow.administration.application.port.in.AdministrationInputPort;
import com.deskflow.administration.application.domain.model.AdministeredAllocationRequestId;
import com.deskflow.administration.application.domain.model.AdministeredAllocationStatus;
import com.deskflow.common.application.domain.model.vo.EmployeeId;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import com.deskflow.employees.application.domain.model.Employee;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/administration")
public class AdministrationController {

    private final AdministrationInputPort administrationInputPort;
    private final EmployeeManagementInputPort employeeManagementInputPort;

    public AdministrationController(AdministrationInputPort administrationInputPort,
                                    EmployeeManagementInputPort employeeManagementInputPort) {
        this.administrationInputPort = administrationInputPort;
        this.employeeManagementInputPort = employeeManagementInputPort;
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AllocationRequestResponseDTO>> listRequests(
            @RequestParam(required = false) String status) {
        AdministeredAllocationStatus statusEnum = status != null ? AdministeredAllocationStatus.valueOf(status) : null;
        return ResponseEntity.ok(enrich(administrationInputPort.listRequests(statusEnum)));
    }

    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID id) {
        administrationInputPort.approveRequest(AdministeredAllocationRequestId.of(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID id, @Valid @RequestBody RejectRequestDTO dto) {
        administrationInputPort.rejectRequest(AdministeredAllocationRequestId.of(id), dto.reason());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{id}/issue")
    public ResponseEntity<Void> issue(@PathVariable UUID id) {
        administrationInputPort.issueResource(AdministeredAllocationRequestId.of(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{id}/return")
    public ResponseEntity<Void> returnResource(@PathVariable UUID id) {
        administrationInputPort.returnResource(AdministeredAllocationRequestId.of(id));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/allocations")
    public ResponseEntity<List<AllocationRequestResponseDTO>> monitorActive() {
        return ResponseEntity.ok(enrich(administrationInputPort.monitorActiveAllocations()));
    }

    private List<AllocationRequestResponseDTO> enrich(List<AdministeredAllocationView> requests) {
        Map<UUID, Employee> cache = requests.stream()
                .map(AdministeredAllocationView::employeeId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> employeeManagementInputPort.getEmployee(EmployeeId.of(id)).orElse(null),
                        (a, b) -> a
                ));
        return requests.stream()
                .map(r -> {
                    Employee emp = cache.get(r.employeeId());
                    return toResponse(r,
                            emp != null ? emp.getFirstName() : null,
                            emp != null ? emp.getLastName() : null);
                })
                .toList();
    }

    private AllocationRequestResponseDTO toResponse(AdministeredAllocationView r, String firstName, String lastName) {
        return new AllocationRequestResponseDTO(
                r.id(),
                r.employeeId(),
                firstName,
                lastName,
                r.resourceId(),
                r.status(),
                r.dateFrom(),
                r.dateTo(),
                r.justification(),
                r.submittedAt(),
                r.issuedAt(),
                r.returnedAt(),
                r.rejectionReason()
        );
    }
}
