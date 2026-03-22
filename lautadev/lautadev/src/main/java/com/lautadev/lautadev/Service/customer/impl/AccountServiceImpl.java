package com.lautadev.lautadev.Service.customer.impl;

import com.lautadev.lautadev.DTO.response.dashboard.AccountDetailResponse;
import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.AccountType;
import com.lautadev.lautadev.Entities.User;
import com.lautadev.lautadev.Exception.ApiException;
import com.lautadev.lautadev.Repositories.AccountRepository;
import com.lautadev.lautadev.Service.customer.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {


    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Account createAccount(User user) {
        String accountId = UUID.randomUUID().toString();

        log.warn("🛠️ Creating account for user ID: {}", user.getId());

        Account account = Account.builder()
                .accountId(accountId)
                .balance(BigDecimal.ZERO)
                .accountType(AccountType.Main)
                .user(user)
                .build();

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public AccountDetailResponse getLoggedInUserAccount() {
        Account account = getUserAccount();
        return new AccountDetailResponse(account.getAccountId(), account.getBalance().stripTrailingZeros(), account.getAccountType());
    }

    @Override
    public AccountDetailResponse getAccountByIndex(User user, String accountIndex) {
        String indexPart = accountIndex.replaceAll("[^0-9]", "");
        String accountTypePart = accountIndex.replaceAll("[0-9]", "");

        int index;
        try {
            index = Integer.parseInt(indexPart);
        } catch (NumberFormatException e) {
            throw ApiException.accountIndexError();
        }

        List<Account> accounts = user.getAccounts();

        if (!accountTypePart.isEmpty()) {
            accounts = accounts.stream()
                    .filter(account -> account.getAccountType().name().equalsIgnoreCase(accountTypePart))
                    .toList();
        }

        if (index < 0 || index >= accounts.size()) {
            throw ApiException.accountIndexError();
        }

        Account account = accounts.get(index);

        return new AccountDetailResponse(
                account.getAccountId(),
                account.getBalance().stripTrailingZeros(),
                account.getAccountType()
        );
    }

    @Override
    public Account getUserAccount() {
        User user = getLoggedInUser();

        return user.getAccounts().stream()
                .filter(account -> account.getAccountType() == AccountType.Main)
                .findFirst()
                .orElseThrow(ApiException::accountNotFound);
    }

    private User validateUserPasswordAndGetUser(String password) {
        User user = getLoggedInUser();

        if(!passwordEncoder.matches(password,user.getPassword())){
            throw ApiException.badCredentials();
        }

        return user;
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
