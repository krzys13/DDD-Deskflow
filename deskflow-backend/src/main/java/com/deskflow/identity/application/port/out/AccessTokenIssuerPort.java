package com.deskflow.identity.application.port.out;

public interface AccessTokenIssuerPort {
    String issueAccessToken(String email, String role);
}
