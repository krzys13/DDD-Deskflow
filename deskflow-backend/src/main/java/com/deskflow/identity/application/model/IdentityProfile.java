package com.deskflow.identity.application.model;

public record IdentityProfile(
        String email,
        String role,
        String firstName,
        String lastName,
        String employeeId
) {}
