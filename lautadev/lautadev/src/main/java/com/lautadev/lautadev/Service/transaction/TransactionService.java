package com.lautadev.lautadev.Service.transaction;

import com.lautadev.lautadev.DTO.request.transaction.TransactionRequest;
import com.lautadev.lautadev.DTO.request.transaction.TransferRequest;
import com.lautadev.lautadev.DTO.response.transaction.TransactionDetail;

import java.util.List;

public interface TransactionService {
    void depositMoney(TransactionRequest transactionRequest);
    void withdrawMoney(TransactionRequest transactionRequest);
    void transferMoney(TransferRequest transferRequest);
    List<TransactionDetail> getTransactions();
}

