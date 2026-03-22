package com.lautadev.lautadev.DTO.request.authentication;

import com.lautadev.lautadev.Util.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterRequest(@NotBlank String name,
                                  @Password String password,
                                  @NotBlank @Email(message = "Invalid email: ${validatedValue}") String email) {
}
