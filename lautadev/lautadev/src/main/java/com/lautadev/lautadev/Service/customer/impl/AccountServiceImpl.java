package com.lautadev.lautadev.Service.customer.impl;

import com.lautadev.lautadev.DTO.response.dashboard.AccountDetailResponse;
import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.AccountType;
import com.lautadev.lautadev.Entities.User;
import com.lautadev.lautadev.Exception.ApiException;
import com.lautadev.lautadev.Repositories.AccountRepository;
import com.lautadev.lautadev.Service.customer.AccountService;
import com.lautadev.lautadev.Service.transaction.impl.InterestServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final InterestServiceImpl interestService;

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
    public void createNewAccount(String accountType, String accountNumber) {
        User user = this.getLoggedInUser();

        Account mainAccount = accountRepository.findByAccountIdAndUser(accountNumber, user)
                .orElseThrow(() -> new ApiException("Main account not found or does not belong to the user", HttpStatus.BAD_REQUEST));

        if (!mainAccount.getAccountType().equals(AccountType.Main)) {
            throw new ApiException("The referenced account is not of type Main", HttpStatus.BAD_REQUEST);
        }

        if (AccountType.valueOf(accountType).equals(AccountType.Invest)) {
            boolean hasInvestAccount = accountRepository.existsByUserAndAccountType(user, AccountType.Invest);
            if (hasInvestAccount) {
                throw new ApiException("The user already has an Invest account", HttpStatus.BAD_REQUEST);
            }
        }

        String accountId = UUID.randomUUID().toString();
        Account newAccount = Account.builder()
                .accountId(accountId)
                .balance(BigDecimal.ZERO)
                .accountType(AccountType.valueOf(accountType))
                .user(user)
                .build();

        accountRepository.save(newAccount);
        interestService.startInterestTask(user);
    }

    @Override
    @Transactional
    public AccountDetailResponse getLoggedInUserAccount() {
        Account account = getUserAccount();
        return new AccountDetailResponse(account.getAccountId(), account.getBalance().stripTrailingZeros(), account.getAccountType());
    }

    @Override
    public AccountDetailResponse getAccountByIndex(String accountIndex) {
        User user = this.getLoggedInUser();

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
            throw ApiException.accountIndexOutOfBounds();
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
