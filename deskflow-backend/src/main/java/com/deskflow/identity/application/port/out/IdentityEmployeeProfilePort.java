package com.deskflow.identity.application.port.out;

import com.deskflow.identity.application.model.IdentityProfile;

import java.util.Optional;

public interface IdentityEmployeeProfilePort {
    Optional<IdentityProfile> findByEmail(String email);
}
