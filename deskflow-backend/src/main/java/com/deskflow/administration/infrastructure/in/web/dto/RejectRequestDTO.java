package com.deskflow.administration.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequestDTO(@NotBlank String reason) {}
