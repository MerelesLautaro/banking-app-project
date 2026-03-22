package com.lautadev.lautadev.Repositories;

import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.AccountType;
import com.lautadev.lautadev.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountId(String accountId);
    Optional<Account> findByAccountIdAndUser(String mainAccountId, User user);
    boolean existsByUserAndAccountType(User user, AccountType accountType);
    Optional<Account> findByUserAndAccountType(User user, AccountType accountType);
}
