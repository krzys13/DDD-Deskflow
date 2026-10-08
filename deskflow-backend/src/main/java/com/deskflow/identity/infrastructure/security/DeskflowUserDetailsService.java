package com.deskflow.identity.infrastructure.security;

import com.deskflow.identity.application.port.out.UserCredentialsPort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeskflowUserDetailsService implements UserDetailsService {

    private final UserCredentialsPort userCredentialsPort;

    public DeskflowUserDetailsService(UserCredentialsPort userCredentialsPort) {
        this.userCredentialsPort = userCredentialsPort;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userCredentialsPort.findByEmail(email)
                .map(credentials -> new User(
                        credentials.email(), credentials.passwordHash(),
                        List.of(new SimpleGrantedAuthority("ROLE_" + credentials.role()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
