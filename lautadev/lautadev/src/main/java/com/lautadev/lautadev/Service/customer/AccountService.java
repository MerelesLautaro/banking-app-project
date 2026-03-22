package com.lautadev.lautadev.Service.customer;

import com.lautadev.lautadev.DTO.response.dashboard.AccountDetailResponse;
import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.User;

import java.util.List;

public interface AccountService {
    Account createAccount(User user);
    // void createNewAccount(User user, String accountType, String accountNumber);
    AccountDetailResponse getLoggedInUserAccount();
    AccountDetailResponse getAccountByIndex(String accountIndex);
    Account getUserAccount();
}
