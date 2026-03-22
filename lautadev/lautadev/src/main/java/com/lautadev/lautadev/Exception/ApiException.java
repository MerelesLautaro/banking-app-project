package com.lautadev.lautadev.Exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException{

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ApiException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException emailAlreadyExist() { return new ApiException("Email already exist", HttpStatus.BAD_REQUEST);}

    public static ApiException accountNotFound() { return new ApiException("Account not found", HttpStatus.NOT_FOUND);}

    public static ApiException userNotFound() { return new ApiException("User not found", HttpStatus.NOT_FOUND);}

    public static ApiException refreshTokenExpired() { return new ApiException("Refresh token expired", HttpStatus.UNAUTHORIZED);}

    public static ApiException authMissing() { return new ApiException("Authentication missing", HttpStatus.UNAUTHORIZED);}

    public static ApiException badCredentials() { return new ApiException("Bad credentials", HttpStatus.UNAUTHORIZED);}

    public static ApiException internalError() { return new ApiException("Internal error", HttpStatus.INTERNAL_SERVER_ERROR);}

    public static ApiException accountIndexError() { return new ApiException("Account index error or invalid format", HttpStatus.BAD_REQUEST);}

    public static ApiException accountIndexOutOfBounds() { return new ApiException("Account index out of bounds", HttpStatus.BAD_REQUEST);}

    public static ApiException accessDenied() {
        return new ApiException("Access Denied", HttpStatus.UNAUTHORIZED);
    }

    // Transactions
    public static ApiException insufficientBalance() {
        return new ApiException("Insufficient balance", HttpStatus.UNAUTHORIZED);
    }

}
