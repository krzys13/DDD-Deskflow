package com.deskflow.identity.application.port.out;

import com.deskflow.identity.application.model.UserCredentials;

import java.util.Optional;

public interface UserCredentialsPort {
    Optional<UserCredentials> findByEmail(String email);
}
