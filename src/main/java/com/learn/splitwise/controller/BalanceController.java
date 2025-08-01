package com.learn.splitwise.controller;

import com.learn.splitwise.dto.GroupBalanceResponse;
import com.learn.splitwise.dto.SettleUpRequest;
import com.learn.splitwise.service.BalanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Settle")
@RestController
@RequestMapping("/api/settle")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BalanceController {

    private final BalanceService balanceService;

    @PostMapping("/group/{groupId}")
    public ResponseEntity<String> settleUp(@PathVariable Long groupId, @RequestBody SettleUpRequest request) {
        balanceService.settleUp(groupId, request);
        return ResponseEntity.ok("Settlement Succesfully");
    }
}
