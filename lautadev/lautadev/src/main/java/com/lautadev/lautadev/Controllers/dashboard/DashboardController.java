package com.lautadev.lautadev.Controllers.dashboard;

import com.lautadev.lautadev.DTO.response.dashboard.AccountDetailResponse;
import com.lautadev.lautadev.DTO.response.dashboard.UserDetailsResponse;
import com.lautadev.lautadev.Service.customer.AccountService;
import com.lautadev.lautadev.Service.customer.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserService userService;
    private final AccountService accountService;

    @GetMapping("/user")
    public ResponseEntity<UserDetailsResponse> getUserInfo() {
        return ResponseEntity.ok(userService.getLoggedInUserDetails());
    }


    @GetMapping("/account")
    public ResponseEntity<AccountDetailResponse> getAccountDetail() {
        return ResponseEntity.ok(accountService.getLoggedInUserAccount());
    }

    @GetMapping("/account/{index}")
    public ResponseEntity<AccountDetailResponse> getAccountByIndex(@PathVariable String index) {
        AccountDetailResponse response = accountService.getAccountByIndex(index);
        return ResponseEntity.ok(response);
    }
}
