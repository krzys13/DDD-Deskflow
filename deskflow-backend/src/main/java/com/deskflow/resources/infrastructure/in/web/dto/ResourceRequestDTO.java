package com.deskflow.resources.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResourceRequestDTO(
        @NotNull String type,
        @NotBlank String name,
        @NotBlank String building,
        int floor,
        @NotBlank String number
) {}
