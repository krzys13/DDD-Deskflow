package com.deskflow.identity.application.service;

import com.deskflow.identity.application.model.IdentityProfile;
import com.deskflow.identity.application.port.out.AccessTokenIssuerPort;
import com.deskflow.identity.application.port.out.CredentialsAuthenticationPort;
import com.deskflow.identity.application.port.out.IdentityEmployeeProfilePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityAccessAppServiceTest {

    @Mock private CredentialsAuthenticationPort credentialsAuthenticationPort;
    @Mock private AccessTokenIssuerPort accessTokenIssuerPort;
    @Mock private IdentityEmployeeProfilePort employeeProfilePort;

    private IdentityAccessAppService appService;

    @BeforeEach
    void setUp() {
        appService = new IdentityAccessAppService(credentialsAuthenticationPort, accessTokenIssuerPort, employeeProfilePort);
    }

    @Test
    void login_authenticates_user_and_returns_profile_with_token() {
        IdentityProfile profile = new IdentityProfile("user@test.pl", "MANAGER", "Jan", "Testowy", "emp-1");
        when(employeeProfilePort.findByEmail("user@test.pl")).thenReturn(Optional.of(profile));
        when(accessTokenIssuerPort.issueAccessToken("user@test.pl", "MANAGER")).thenReturn("token-123");

        var identity = appService.login("user@test.pl", "password");

        verify(credentialsAuthenticationPort).authenticate("user@test.pl", "password");
        assertThat(identity.token()).isEqualTo("token-123");
        assertThat(identity.email()).isEqualTo("user@test.pl");
        assertThat(identity.role()).isEqualTo("MANAGER");
        assertThat(identity.firstName()).isEqualTo("Jan");
        assertThat(identity.lastName()).isEqualTo("Testowy");
        assertThat(identity.employeeId()).isEqualTo("emp-1");
    }

    @Test
    void login_throws_when_authenticated_user_has_no_employee_profile() {
        when(employeeProfilePort.findByEmail("missing@test.pl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appService.login("missing@test.pl", "password"))
                .isInstanceOf(NoSuchElementException.class);

        verify(credentialsAuthenticationPort).authenticate("missing@test.pl", "password");
    }

    @Test
    void current_identity_returns_profile_by_email() {
        IdentityProfile profile = new IdentityProfile("user@test.pl", "EMPLOYEE", "Anna", "Nowak", "emp-2");
        when(employeeProfilePort.findByEmail("user@test.pl")).thenReturn(Optional.of(profile));

        assertThat(appService.currentIdentity("user@test.pl")).contains(profile);
    }
}
