package com.learn.splitwise.controller;

import com.learn.splitwise.dto.NetBalanceResponse;
import com.learn.splitwise.dto.UserDashboardResponse;
import com.learn.splitwise.security.JwtService;
import com.learn.splitwise.service.BalanceService;
import com.learn.splitwise.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final BalanceService balanceService;
    private final JwtService jwtService;
    private final UserService userService;

    @GetMapping("/me")
    public String getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return "Logged in as : " + userDetails.getUsername();
    }

    @GetMapping("/{userId}/net-balances")
    public ResponseEntity<List<NetBalanceResponse>> getUserNetBalances(@PathVariable Long userId) {
        List<NetBalanceResponse> results = balanceService.getNetBalanceForUser(userId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardResponse> getDashboard(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);
        UserDashboardResponse response = userService.getDashboard(email);
        return ResponseEntity.ok(response);

    }
}
