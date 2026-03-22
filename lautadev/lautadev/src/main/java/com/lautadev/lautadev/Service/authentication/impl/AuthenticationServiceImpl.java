package com.lautadev.lautadev.Service.authentication.impl;

import com.lautadev.lautadev.DTO.request.authentication.LoginRequest;
import com.lautadev.lautadev.DTO.request.authentication.UserRegisterRequest;
import com.lautadev.lautadev.DTO.response.dashboard.UserDetailsResponse;
import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.AccountType;
import com.lautadev.lautadev.Entities.Token;
import com.lautadev.lautadev.Entities.User;
import com.lautadev.lautadev.Exception.ApiException;
import com.lautadev.lautadev.Repositories.UserRepository;
import com.lautadev.lautadev.Security.JWTBlacklistManager;
import com.lautadev.lautadev.Service.authentication.AuthenticationService;
import com.lautadev.lautadev.Service.authentication.TokenService;
import com.lautadev.lautadev.Service.customer.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AccountService accountService;
    private final JWTBlacklistManager jwtBlacklistManager;

    @Override
    @Transactional
    public UserDetailsResponse registerUser(UserRegisterRequest userRegisterRequest) {
        log.warn("➡️ Starting user registration for email: {}", userRegisterRequest.email());

        validateEmail(userRegisterRequest);

        String encodedPassword = passwordEncoder.encode(userRegisterRequest.password());
        log.warn("🔐 Password encoded");

        User user = createUserAndAccount(userRegisterRequest, encodedPassword);
        log.warn("👤 User created with ID: {}", user.getId());

        if (user.getAccounts() == null) {
            log.warn("❌ user.getAccounts() is NULL");
        } else {
            log.warn("📊 Accounts count: {}", user.getAccounts().size());
            user.getAccounts().forEach(acc ->
                    log.info("➡️ Account -> id: {}, type: {}", acc.getAccountId(), acc.getAccountType())
            );
        }

        Account mainAccount = user.getAccounts().stream()
                .filter(account -> account.getAccountType() == AccountType.Main)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("❌ No MAIN account found!");
                    return ApiException.internalError();
                });

        log.warn("✅ Main account found: {}", mainAccount.getAccountId());

        return createUserResponse(
                userRegisterRequest,
                encodedPassword,
                mainAccount.getAccountId(),
                mainAccount.getAccountType().name()
        );
    }

    @Override
    public Token login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.identifier(), loginRequest.password()));

            User userDetails = (User) authentication.getPrincipal();

            String token = tokenService.generateToken(userDetails);

            return new Token(token);

        } catch (BadCredentialsException e) {
            throw ApiException.badCredentials();
        }
    }

    @Override
    public void logout(String token) {
        jwtBlacklistManager.addTokenToBlackList(token);
    }

    private void validateEmail(UserRegisterRequest userRegisterRequest) {
        if (userRepository.existsByEmailIgnoreCase(userRegisterRequest.email())) {
            throw ApiException.emailAlreadyExist();
        }
    }

    private UserDetailsResponse createUserResponse(UserRegisterRequest userRegisterRequest,
                                                   String encodedPassword,
                                                   String accountNumber,
                                                   String accountType) {

        return new UserDetailsResponse(
                userRegisterRequest.name(),
                userRegisterRequest.email(),
                accountNumber,
                accountType,
                encodedPassword
        );
    }

    private User createUserAndAccount(UserRegisterRequest userRegisterRequest, String encodedPassword) {

        User user = User.builder()
                .name(userRegisterRequest.name())
                .email(userRegisterRequest.email())
                .password(encodedPassword)
                .build();

        user = userRepository.save(user);

        Account account = accountService.createAccount(user);
        user.addAccount(account);

        return user;
    }
}
