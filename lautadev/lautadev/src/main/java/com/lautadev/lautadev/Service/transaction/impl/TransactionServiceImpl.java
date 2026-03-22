package com.lautadev.lautadev.Service.transaction.impl;

import com.lautadev.lautadev.DTO.request.transaction.TransactionRequest;
import com.lautadev.lautadev.DTO.request.transaction.TransferRequest;
import com.lautadev.lautadev.DTO.response.transaction.TransactionDetail;
import com.lautadev.lautadev.Entities.*;
import com.lautadev.lautadev.Exception.ApiException;
import com.lautadev.lautadev.Repositories.AccountRepository;
import com.lautadev.lautadev.Repositories.TransactionRepository;
import com.lautadev.lautadev.Service.customer.AccountService;
import com.lautadev.lautadev.Service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void depositMoney(TransactionRequest transactionRequest) {
        Account account = getAccount();

        BigDecimal depositAmount = transactionRequest.amount();

        BigDecimal commission = BigDecimal.ZERO;
        if (depositAmount.compareTo(BigDecimal.valueOf(50000)) > 0) {
            commission = depositAmount.multiply(BigDecimal.valueOf(0.02));
        }

        BigDecimal finalAmount = depositAmount.subtract(commission);

        BigDecimal originalBalance = account.getBalance();
        account.setBalance(originalBalance.add(finalAmount));

        accountRepository.save(account);
        accountRepository.flush();

        Transaction transaction = saveTransaction(depositAmount, account, null,
                TransactionType.CASH_DEPOSIT,
                TransactionStatus.PENDING);

        Account updatedAccount = accountRepository.findById(account.getId())
                .orElseThrow(ApiException::accountNotFound);

        BigDecimal expectedBalance = originalBalance.add(finalAmount);

        if (updatedAccount.getBalance().compareTo(expectedBalance) == 0) {
            transaction.setTransactionStatus(TransactionStatus.APPROVED);
            transactionRepository.save(transaction);
        } else {
            log.error("Deposit failed: Balance mismatch.");
            throw new RuntimeException("Error in depositing the funds. Please try again.");
        }
    }

    @Override
    @Transactional
    public void withdrawMoney(TransactionRequest transactionRequest) {
        Account account = getAccount();
        BigDecimal withdrawAmount = transactionRequest.amount();

        BigDecimal commission = BigDecimal.ZERO;
        if (withdrawAmount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            commission = withdrawAmount.multiply(BigDecimal.valueOf(0.01));
        }
        BigDecimal totalDeduction = withdrawAmount.add(commission);

        subtractMoney(totalDeduction, account);

        saveTransaction(withdrawAmount, account, null, TransactionType.CASH_WITHDRAWAL, TransactionStatus.PENDING);
    }

    @Override
    @Transactional
    public void transferMoney(TransferRequest transferRequest) {
        Account sourceAccount = getAccount();

        Account destinationAccount = accountRepository.findByAccountId(transferRequest.targetAccountNumber())
                .orElseThrow(ApiException::accessDenied);

        if (destinationAccount.getAccountType() == AccountType.Invest && sourceAccount.getAccountType() != AccountType.Main) {
            throw new ApiException("Only main accounts can transfer to investment accounts", HttpStatus.UNAUTHORIZED);
        }

        if (sourceAccount.getAccountId().equals(destinationAccount.getAccountId())) {
            throw new ApiException("Cannot transfer to the same account", HttpStatus.UNAUTHORIZED);
        }

        BigDecimal transferAmount = transferRequest.amount();

        boolean isFraud = transferAmount.compareTo(BigDecimal.valueOf(80000)) > 0;

        TransactionStatus status = isFraud ? TransactionStatus.FRAUD : TransactionStatus.PENDING;
        saveTransaction(transferAmount, sourceAccount, destinationAccount, TransactionType.CASH_TRANSFER, status);

        if (checkFrequentTransfers(sourceAccount, destinationAccount)) {
            status = TransactionStatus.FRAUD;
            log.warn("Potential fraud detected: Multiple transfers to the same account within 5 seconds.");
        }

        if (status == TransactionStatus.FRAUD) {
            throw new ApiException("Transaction marked as fraud", HttpStatus.UNAUTHORIZED);
        }

        subtractMoney(transferAmount, sourceAccount);
        addMoney(destinationAccount, transferAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDetail> getTransactions() {
        Account account = accountService.getUserAccount();

        return transactionRepository.findBySourceAccount(account).stream()
                .map(Transaction::toDetail)
                .toList();
    }


    private Account getAccount() {
        return accountService.getUserAccount();
    }

    private Transaction saveTransaction(BigDecimal amount, Account sourceAccount, Account targetAccount,
                                        TransactionType transactionType, TransactionStatus transactionStatus) {

        Transaction transaction = Transaction.builder()
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .amount(amount)
                .transactionType(transactionType)
                .transactionStatus(transactionStatus)
                .transactionDate(Instant.now())
                .build();

        return transactionRepository.save(transaction);
    }

    private void subtractMoney(BigDecimal value, Account account) {
        BigDecimal newBalance = account.getBalance().subtract(value);
        boolean isNegativeBalance = newBalance.compareTo(BigDecimal.ZERO) < 0;

        if (isNegativeBalance) {
            log.warn("Insufficient balance to perform the subtraction, balance {}, subtract intent {}",
                    account.getBalance(), value);
            throw ApiException.insufficientBalance();
        }
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    private void addMoney(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }


    private boolean checkFrequentTransfers(Account sourceAccount, Account targetAccount) {
        Instant fiveSecondsAgo = Instant.now().minusSeconds(5);

        long transferCount = transactionRepository.countBySourceAccountAndTargetAccountAndTransactionDateAfter(
                sourceAccount, targetAccount, fiveSecondsAgo);

        return transferCount >= 4;
    }
}
