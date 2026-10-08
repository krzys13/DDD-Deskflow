package com.deskflow.identity.infrastructure.security;

import com.deskflow.identity.application.model.UserCredentials;
import com.deskflow.identity.application.port.out.UserCredentialsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeskflowUserDetailsServiceTest {

    @Mock private UserCredentialsPort userCredentialsPort;

    private DeskflowUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new DeskflowUserDetailsService(userCredentialsPort);
    }

    @Test
    void loadUserByUsername_returns_user_details() {
        when(userCredentialsPort.findByEmail("test@company.test"))
                .thenReturn(Optional.of(new UserCredentials("test@company.test", "hashed_pass", "EMPLOYEE")));

        var userDetails = service.loadUserByUsername("test@company.test");

        assertThat(userDetails.getUsername()).isEqualTo("test@company.test");
        assertThat(userDetails.getPassword()).isEqualTo("hashed_pass");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_EMPLOYEE");
    }

    @Test
    void loadUserByUsername_throws_when_not_found() {
        when(userCredentialsPort.findByEmail("missing@company.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@company.test"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@company.test");
    }
}
