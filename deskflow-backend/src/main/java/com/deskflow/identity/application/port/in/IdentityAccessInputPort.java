package com.deskflow.identity.application.port.in;

import com.deskflow.identity.application.model.AuthenticatedIdentity;
import com.deskflow.identity.application.model.IdentityProfile;

import java.util.Optional;

public interface IdentityAccessInputPort {
    AuthenticatedIdentity login(String email, String password);

    Optional<IdentityProfile> currentIdentity(String email);
}
