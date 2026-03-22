package com.lautadev.lautadev.Service.authentication;

import com.lautadev.lautadev.DTO.request.authentication.LoginRequest;
import com.lautadev.lautadev.DTO.request.authentication.UserRegisterRequest;
import com.lautadev.lautadev.DTO.response.dashboard.UserDetailsResponse;
import com.lautadev.lautadev.Entities.Token;

public interface AuthenticationService {
    UserDetailsResponse registerUser(UserRegisterRequest userRegisterRequest);
    Token login(LoginRequest loginRequest);
    void logout(String token);
}