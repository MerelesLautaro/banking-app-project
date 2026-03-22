package com.lautadev.lautadev.DTO.request.authentication;

import com.lautadev.lautadev.Util.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String identifier,
                           @Password String password) {
}
