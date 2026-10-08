package com.deskflow.resources.infrastructure.in.web;

import com.deskflow.resources.infrastructure.in.web.dto.ResourceRequestDTO;
import com.deskflow.resources.application.port.in.ResourceManagementInputPort;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import com.deskflow.common.application.domain.model.vo.ResourceType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceManagementInputPort resourceManagementInputPort;

    public ResourceController(ResourceManagementInputPort resourceManagementInputPort) {
        this.resourceManagementInputPort = resourceManagementInputPort;
    }

    @PostMapping
    public ResponseEntity<?> addResource(@Valid @RequestBody ResourceRequestDTO dto) {
        ResourceId id = resourceManagementInputPort.addResource(
                ResourceType.valueOf(dto.type()), dto.name(), dto.building(), dto.floor(), dto.number());
        return ResponseEntity.created(URI.create("/api/resources/" + id))
                .body(Map.of("resourceId", id.getValue()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateResource(@PathVariable UUID id, @Valid @RequestBody ResourceRequestDTO dto) {
        resourceManagementInputPort.updateResource(ResourceId.of(id), dto.name(), dto.building(), dto.floor(), dto.number());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unavailable")
    public ResponseEntity<Void> markUnavailable(@PathVariable UUID id) {
        resourceManagementInputPort.markUnavailable(ResourceId.of(id));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/available")
    public ResponseEntity<Void> markAvailable(@PathVariable UUID id) {
        resourceManagementInputPort.markAvailable(ResourceId.of(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        resourceManagementInputPort.deactivateResource(ResourceId.of(id));
        return ResponseEntity.ok().build();
    }
}
