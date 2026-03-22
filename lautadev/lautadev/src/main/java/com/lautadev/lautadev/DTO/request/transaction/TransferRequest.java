package com.lautadev.lautadev.DTO.request.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(@NotNull @Positive BigDecimal amount,
                              @NotBlank String targetAccountNumber) {
}
