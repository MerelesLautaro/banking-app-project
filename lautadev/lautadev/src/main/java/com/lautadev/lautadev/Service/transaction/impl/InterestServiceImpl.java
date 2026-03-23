package com.lautadev.lautadev.Service.transaction.impl;

import com.lautadev.lautadev.Entities.Account;
import com.lautadev.lautadev.Entities.AccountType;
import com.lautadev.lautadev.Entities.User;
import com.lautadev.lautadev.Repositories.AccountRepository;
import com.lautadev.lautadev.Service.transaction.InterestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class InterestServiceImpl  implements InterestService {

    private final AccountRepository accountRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks;

    public InterestServiceImpl(AccountRepository accountRepository, ThreadPoolTaskScheduler taskScheduler){
        this.accountRepository = accountRepository;
        this.taskScheduler = taskScheduler;
        this.scheduledTasks = new HashMap<>();
    }

    @Override
    public void startInterestTask(User user) {
        Optional<Account> accountInterest = accountRepository.findByUserAndAccountType(user, AccountType.Invest);

        if (accountInterest.isPresent()) {
            Account investAccount = accountInterest.get();

            log.info("Interest task started for investment account {} (Account number: {}). Interest will be applied every 10 seconds.",
                    investAccount.getAccountId(), investAccount.getAccountId());

            if (investAccount.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                Runnable task = () -> {
                    try {
                        this.applyInvestmentInterest(user);
                    } catch (RuntimeException e) {
                        log.warn("Stopping interest task due to an error for user {}: {}", user.getUsername(), e.getMessage());
                        cancelInterestTask(user);
                    }
                };
                scheduleInterestTask(user, task, 10);
            } else {
                log.info("Account balance is 0 for user {}. Interest task not started.", user.getUsername());
            }
        } else {
            log.warn("No investment account found for user {}. Interest task not started.", user.getUsername());
        }
    }

    public void scheduleInterestTask(User user, Runnable task, long delayInSeconds) {
        Long userId = user.getId();
        ScheduledFuture<?> existingTask = scheduledTasks.get(userId);

        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(true);
            scheduledTasks.remove(userId);
        }

        ScheduledFuture<?> futureTask = taskScheduler.scheduleWithFixedDelay(
                task, Instant.now(), Duration.ofSeconds(delayInSeconds));

        scheduledTasks.put(userId, futureTask);
    }

    public void applyInvestmentInterest(User user) {
        Optional<Account> accountInterest = accountRepository.findByUserAndAccountType(user, AccountType.Invest);

        if (accountInterest.isPresent()) {
            Account investAccount = accountInterest.get();

            if (investAccount.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentBalance = investAccount.getBalance();
                BigDecimal interest = currentBalance.multiply(BigDecimal.valueOf(0.10));
                BigDecimal newBalance = currentBalance.add(interest);

                investAccount.setBalance(newBalance);
                accountRepository.save(investAccount);

                log.info("Applied 10% interest to investment account {} (Account number: {}). " +
                                "Old balance: {}, Interest applied: {}, New balance: {}",
                        investAccount.getAccountId(), investAccount.getAccountId(),
                        currentBalance, interest, newBalance);
            }
        } else {
            log.warn("No investment account found for user {}. No interest applied.", user.getUsername());
        }
    }

    public void cancelInterestTask(User user) {
        Long userId = user.getId();
        ScheduledFuture<?> existingTask = scheduledTasks.get(userId);
        if (existingTask != null) {
            existingTask.cancel(true);
            scheduledTasks.remove(userId);
        }
    }
}
