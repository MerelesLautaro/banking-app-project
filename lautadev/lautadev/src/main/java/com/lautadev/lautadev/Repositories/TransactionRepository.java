package com.lautadev.lautadev.Repositories;

import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    long countBySourceAccountAndTargetAccountAndTransactionDateAfter(
            Account sourceAccount, Account targetAccount, Instant timestamp);
    List<Transaction> findBySourceAccount(Account sourceAccount);
}
