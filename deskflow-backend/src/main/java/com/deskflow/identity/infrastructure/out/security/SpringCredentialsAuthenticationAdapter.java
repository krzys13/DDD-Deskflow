package com.deskflow.identity.infrastructure.out.security;

import com.deskflow.identity.application.port.out.CredentialsAuthenticationPort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SpringCredentialsAuthenticationAdapter implements CredentialsAuthenticationPort {

    private final AuthenticationManager authenticationManager;

    public SpringCredentialsAuthenticationAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }
}
