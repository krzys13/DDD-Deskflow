package com.deskflow.allocation.infrastructure.in.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AllocationRequestDTO(
        @NotNull UUID resourceId,
        @NotNull @FutureOrPresent LocalDate dateFrom,
        @NotNull LocalDate dateTo,
        String justification
) {}
