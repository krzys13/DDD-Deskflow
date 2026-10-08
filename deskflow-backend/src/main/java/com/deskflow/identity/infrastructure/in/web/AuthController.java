package com.deskflow.identity.infrastructure.in.web;

import com.deskflow.identity.application.model.AuthenticatedIdentity;
import com.deskflow.identity.application.model.IdentityProfile;
import com.deskflow.identity.application.port.in.IdentityAccessInputPort;
import com.deskflow.identity.infrastructure.in.web.dto.LoginRequest;
import com.deskflow.identity.infrastructure.in.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IdentityAccessInputPort identityAccessInputPort;

    public AuthController(IdentityAccessInputPort identityAccessInputPort) {
        this.identityAccessInputPort = identityAccessInputPort;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(toResponse(identityAccessInputPort.login(req.email(), req.password())));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return identityAccessInputPort.currentIdentity(auth.getName())
                .<ResponseEntity<?>>map(profile -> ResponseEntity.ok(toResponse(profile)))
                .orElse(ResponseEntity.notFound().build());
    }

    private LoginResponse toResponse(AuthenticatedIdentity identity) {
        return new LoginResponse(
                identity.token(), identity.email(), identity.role(),
                identity.firstName(), identity.lastName(), identity.employeeId()
        );
    }

    private LoginResponse toResponse(IdentityProfile profile) {
        return new LoginResponse(
                null, profile.email(), profile.role(),
                profile.firstName(), profile.lastName(), profile.employeeId()
        );
    }
}
