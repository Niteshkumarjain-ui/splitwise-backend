package com.learn.splitwise.controller;

import com.learn.splitwise.dto.NetBalanceResponse;
import com.learn.splitwise.service.BalanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final BalanceService balanceService;

    @GetMapping("/me")
    public String getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return "Logged in as : " + userDetails.getUsername();
    }

    @GetMapping("/{userId}/net-balances")
    public ResponseEntity<List<NetBalanceResponse>> getUserNetBalances(@PathVariable Long userId) {
        List<NetBalanceResponse> results = balanceService.getNetBalanceForUser(userId);
        return ResponseEntity.ok(results);
    }
}
