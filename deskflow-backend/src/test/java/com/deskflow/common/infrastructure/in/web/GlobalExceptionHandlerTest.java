package com.deskflow.common.infrastructure.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void illegalState_returns_409() {
        var ex = new IllegalStateException("Resource is not available");

        ResponseEntity<?> response = handler.handleBusinessRule(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Resource is not available"));
    }

    @Test
    void illegalArgument_returns_400() {
        var ex = new IllegalArgumentException("Invalid resource type");

        ResponseEntity<?> response = handler.handleBadArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Invalid resource type"));
    }

    @Test
    void noSuchElement_returns_404() {
        var ex = new NoSuchElementException("Employee not found");

        ResponseEntity<?> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "Employee not found"));
    }

    @Test
    void validationError_returns_400_with_details() throws Exception {
        var method = GlobalExceptionHandlerTest.class.getDeclaredMethod("validationError_returns_400_with_details");
        var param = new MethodParameter(method, -1);
        var binding = new BeanPropertyBindingResult(new Object(), "object");
        var ex = new MethodArgumentNotValidException(param, binding);

        ResponseEntity<?> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsKey("error");
        assertThat(body).containsKey("details");
    }
}
