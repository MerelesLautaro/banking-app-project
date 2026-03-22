package com.lautadev.lautadev.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false, precision = 38, scale = 16)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
