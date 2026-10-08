package com.deskflow.identity.infrastructure.in.web.dto;

public record LoginResponse(String token, String email, String role, String firstName, String lastName, String employeeId) {}
