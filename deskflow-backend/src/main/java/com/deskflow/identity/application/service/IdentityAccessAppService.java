package com.deskflow.identity.application.service;

import com.deskflow.identity.application.model.AuthenticatedIdentity;
import com.deskflow.identity.application.model.IdentityProfile;
import com.deskflow.identity.application.port.in.IdentityAccessInputPort;
import com.deskflow.identity.application.port.out.AccessTokenIssuerPort;
import com.deskflow.identity.application.port.out.CredentialsAuthenticationPort;
import com.deskflow.identity.application.port.out.IdentityEmployeeProfilePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class IdentityAccessAppService implements IdentityAccessInputPort {

    private final CredentialsAuthenticationPort credentialsAuthenticationPort;
    private final AccessTokenIssuerPort accessTokenIssuerPort;
    private final IdentityEmployeeProfilePort employeeProfilePort;

    public IdentityAccessAppService(CredentialsAuthenticationPort credentialsAuthenticationPort,
                                    AccessTokenIssuerPort accessTokenIssuerPort,
                                    IdentityEmployeeProfilePort employeeProfilePort) {
        this.credentialsAuthenticationPort = credentialsAuthenticationPort;
        this.accessTokenIssuerPort = accessTokenIssuerPort;
        this.employeeProfilePort = employeeProfilePort;
    }

    @Override
    public AuthenticatedIdentity login(String email, String password) {
        credentialsAuthenticationPort.authenticate(email, password);
        IdentityProfile profile = employeeProfilePort.findByEmail(email).orElseThrow();
        String token = accessTokenIssuerPort.issueAccessToken(email, profile.role());
        return new AuthenticatedIdentity(
                token, profile.email(), profile.role(),
                profile.firstName(), profile.lastName(), profile.employeeId()
        );
    }

    @Override
    public Optional<IdentityProfile> currentIdentity(String email) {
        return employeeProfilePort.findByEmail(email);
    }
}
