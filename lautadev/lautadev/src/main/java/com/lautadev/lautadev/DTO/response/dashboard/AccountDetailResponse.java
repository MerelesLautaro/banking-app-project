package com.lautadev.lautadev.DTO.response.dashboard;

import com.lautadev.lautadev.Entities.AccountType;

import java.math.BigDecimal;

public record AccountDetailResponse(String accountNumber, BigDecimal balance, AccountType accountType) {
}
