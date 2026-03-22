package com.lautadev.lautadev.Exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ApiException {

    public InvalidPasswordException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public static InvalidPasswordException lengthTooShort() {
        return new InvalidPasswordException("Password must be at least 8 characters long");
    }

    public static InvalidPasswordException lengthTooLong() {
        return new InvalidPasswordException("Password must be less than 128 characters long");
    }

    public static InvalidPasswordException missingUppercase() {
        return new InvalidPasswordException("Password must contain at least one uppercase letter");
    }

    public static InvalidPasswordException missingDigitAndSpecialChar() {
        return new InvalidPasswordException("Password must contain at least one digit and one special character");
    }

    public static InvalidPasswordException missingSpecialChar() {
        return new InvalidPasswordException("Password must contain at least one special character");
    }

    public static InvalidPasswordException containsWhitespace() {
        return new InvalidPasswordException("Password cannot contain whitespace");
    }
}
