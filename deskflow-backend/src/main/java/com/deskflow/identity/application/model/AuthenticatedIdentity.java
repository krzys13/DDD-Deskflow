package com.deskflow.identity.application.model;

public record AuthenticatedIdentity(
        String token,
        String email,
        String role,
        String firstName,
        String lastName,
        String employeeId
) {}
