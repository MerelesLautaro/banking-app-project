package com.lautadev.lautadev.Entities;

import com.lautadev.lautadev.DTO.response.transaction.TransactionDetail;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "transaction")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Account sourceAccount;

    @ManyToOne
    private Account targetAccount;

    @Column(nullable = false, updatable = false)
    private Instant transactionDate;

    @Column(nullable = false, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    public TransactionDetail toDetail() {
        String targetAccountNumber;

        if (targetAccount != null) {
            targetAccountNumber = targetAccount.getAccountId();
        } else {
            targetAccountNumber = "N/A";
        }
        return new TransactionDetail(id, amount, transactionType.toString(), transactionStatus.toString(), transactionDate.toEpochMilli(),
                sourceAccount.getAccountId(), targetAccountNumber);
    }
}
