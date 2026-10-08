package com.deskflow.identity.application.model;

public record UserCredentials(String email, String passwordHash, String role) {}
