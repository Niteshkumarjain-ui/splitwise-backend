package com.learn.splitwise.controller;

import com.learn.splitwise.dto.CreateExpenseRequest;
import com.learn.splitwise.dto.ExpenseDetailsResponse;
import com.learn.splitwise.dto.UpdateExpenseRequest;
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

    @PutMapping("/{expenseId}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long expenseId, @RequestBody UpdateExpenseRequest request) {
        Expense updated = expenseService.updateExpense(expenseId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.ok("Expense deleted successfully");
    }

    @GetMapping("{expenseId}")
    public ResponseEntity<ExpenseDetailsResponse> getExpenseDetails(@PathVariable Long expenseId) {
        ExpenseDetailsResponse response = expenseService.getExpenseDetails(expenseId);
        return ResponseEntity.ok(response);
    }
}
