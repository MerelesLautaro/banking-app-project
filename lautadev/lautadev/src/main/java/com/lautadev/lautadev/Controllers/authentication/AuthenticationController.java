package com.lautadev.lautadev.Controllers.authentication;

import com.lautadev.lautadev.DTO.request.authentication.LoginRequest;
import com.lautadev.lautadev.DTO.request.authentication.UserRegisterRequest;
import com.lautadev.lautadev.DTO.response.dashboard.UserDetailsResponse;
import com.lautadev.lautadev.Entities.Token;
import com.lautadev.lautadev.Service.authentication.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserDetailsResponse> registerUser(@RequestBody @Valid
                                                            UserRegisterRequest userRegisterRequest) throws BadRequestException {
        return ResponseEntity.ok(authenticationService.registerUser(userRegisterRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody @Valid
                                       LoginRequest loginRequest) {


        return ResponseEntity.ok(authenticationService.login(loginRequest));
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                       String authorization) {

        authenticationService.logout(authorization);
        return ResponseEntity.ok().build();
    }
}
