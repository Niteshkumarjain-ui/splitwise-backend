package com.learn.splitwise.controller;

import com.learn.splitwise.dto.CreateExpenseRequest;
import com.learn.splitwise.model.Expense;
import com.learn.splitwise.service.ExpenseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Expense")
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody CreateExpenseRequest request) {
        Expense expense = expenseService.createExpense(request);
        return ResponseEntity.ok(expense);
    }
}
