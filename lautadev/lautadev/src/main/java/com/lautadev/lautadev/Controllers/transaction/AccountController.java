package com.lautadev.lautadev.Controllers.transaction;

import com.lautadev.lautadev.DTO.GenericResponse;
import com.lautadev.lautadev.DTO.request.authentication.CreateAccountRequest;
import com.lautadev.lautadev.DTO.request.transaction.TransactionRequest;
import com.lautadev.lautadev.DTO.request.transaction.TransferRequest;
import com.lautadev.lautadev.DTO.response.transaction.TransactionDetail;
import com.lautadev.lautadev.Service.customer.AccountService;
import com.lautadev.lautadev.Service.transaction.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {

    private final TransactionService transactionService;
    private final AccountService accountService;


    @PostMapping("/create")
    public ResponseEntity<String> createNewAccount(@Valid @RequestBody CreateAccountRequest request) {
        accountService.createNewAccount(request.accountType() , request.accountNumber());
        return ResponseEntity.ok("New account added successfully for user");
    }

    @PostMapping("/deposit")
    public ResponseEntity<GenericResponse> depositMoney(@Valid @RequestBody TransactionRequest transactionRequest) {
        transactionService.depositMoney(transactionRequest);
        return ResponseEntity.ok(new GenericResponse("Cash deposited successfully"));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<GenericResponse> withdrawMoney(@Valid @RequestBody TransactionRequest transactionRequest) {
        transactionService.withdrawMoney(transactionRequest);
        return ResponseEntity.ok(new GenericResponse("Cash withdrawn successfully"));
    }

    @PostMapping("/fund-transfer")
    public ResponseEntity<GenericResponse> transfer(@Valid @RequestBody TransferRequest transferRequest) {
        transactionService.transferMoney(transferRequest);
        return ResponseEntity.ok(new GenericResponse("Fund transferred successfully"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDetail>> getTransactions(){
        return ResponseEntity.ok(transactionService.getTransactions());
    }

}
