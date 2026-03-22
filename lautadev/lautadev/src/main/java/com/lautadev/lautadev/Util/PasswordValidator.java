package com.lautadev.lautadev.Util;

import com.lautadev.lautadev.Exception.InvalidPasswordException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || !password.matches("^.*[A-Z]+.*$")) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }

        if (!password.matches("^.*\\d+.*$")) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }

        if (!password.matches("^(?=.*[\\W_]).*$")) {
            throw new InvalidPasswordException("Password must contain at least one special character");
        }

        if (!password.matches("^\\S*$")) {
            throw new InvalidPasswordException("Password cannot contain whitespace");
        }

        if (password.length() >= 128) {
            throw new InvalidPasswordException("Password must be less than 128 characters long");
        }

        if (password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long");
        }

        return true;
    }
}
