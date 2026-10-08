package com.deskflow.identity.application.port.out;

public interface CredentialsAuthenticationPort {
    void authenticate(String email, String password);
}
