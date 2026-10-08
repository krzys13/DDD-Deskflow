package com.deskflow.identity.infrastructure.out.employees;

import com.deskflow.employees.application.model.EmployeeCredentials;
import com.deskflow.employees.application.port.in.EmployeeManagementInputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeUserCredentialsAdapterTest {

    @Mock private EmployeeManagementInputPort employeeManagementInputPort;

    private EmployeeUserCredentialsAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EmployeeUserCredentialsAdapter(employeeManagementInputPort);
    }

    @Test
    void findByEmail_returns_mapped_credentials_when_found() {
        var employeeCredentials = new EmployeeCredentials("test@company.test", "hashed_pass", "EMPLOYEE");
        when(employeeManagementInputPort.getEmployeeCredentialsByEmail("test@company.test"))
                .thenReturn(Optional.of(employeeCredentials));

        var result = adapter.findByEmail("test@company.test");

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("test@company.test");
        assertThat(result.get().passwordHash()).isEqualTo("hashed_pass");
        assertThat(result.get().role()).isEqualTo("EMPLOYEE");
    }

    @Test
    void findByEmail_returns_empty_when_not_found() {
        when(employeeManagementInputPort.getEmployeeCredentialsByEmail("missing@company.test"))
                .thenReturn(Optional.empty());

        var result = adapter.findByEmail("missing@company.test");

        assertThat(result).isEmpty();
    }
}
