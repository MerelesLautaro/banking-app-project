package com.lautadev.lautadev.Service.customer.impl;

import com.lautadev.lautadev.DTO.response.dashboard.UserDetailsResponse;
import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.AccountType;
import com.lautadev.lautadev.Entities.User;
import com.lautadev.lautadev.Exception.ApiException;
import com.lautadev.lautadev.Repositories.UserRepository;
import com.lautadev.lautadev.Service.customer.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserDetailsService, UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new ApiException("User not found for the given identifier: " + username,
                                new UsernameNotFoundException(username), HttpStatus.BAD_REQUEST));
    }

    @Override
    @Transactional
    public UserDetailsResponse getLoggedInUserDetails() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw ApiException.authMissing();
        }

        User user = (User) authentication.getPrincipal();
        if (user == null) {
            throw ApiException.userNotFound();
        }

        if (user.getAccounts() == null || user.getAccounts().isEmpty()) {
            throw ApiException.accountNotFound();
        }

        Account mainAccount = user.getAccounts().stream()
                .filter(account -> account.getAccountType() == AccountType.Main)
                .findFirst()
                .orElseThrow(ApiException::accountNotFound);

        return new UserDetailsResponse(
                user.getName(),
                user.getEmail(),
                mainAccount.getAccountId(),
                mainAccount.getAccountType().name(),
                user.getPassword()
        );
    }
}
