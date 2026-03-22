package com.lautadev.lautadev.DTO.response.dashboard;

public record UserDetailsResponse(String name,
                                  String email,
                                  String accountNumber,
                                  String accountType,
                                  String hashedPassword) {
}

