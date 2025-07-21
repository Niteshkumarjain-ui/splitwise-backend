package com.learn.splitwise.service;

import com.learn.splitwise.dto.CreateExpenseRequest;
import com.learn.splitwise.dto.ExpenseResponse;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.*;
import com.learn.splitwise.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final GroupRepository groupRepository;
    private final SplitRepository splitRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final BalanceRepository balanceRepository;

    public Expense createExpense(CreateExpenseRequest request) {

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomException("Group Not found", HttpStatus.NOT_FOUND));

        User paidBy = userRepository.findById(request.getPaidByUserId())
                .orElseThrow(() -> new CustomException("Payer Not found", HttpStatus.NOT_FOUND));

        List<User> splitAmong = userRepository.findAllById(request.getSplitAmongUserIds());

        if (splitAmong.size() != request.getSplitAmongUserIds().size()) {
            throw new CustomException("one or more split users are invalid", HttpStatus.BAD_REQUEST);
        }

        // 1. Save Expense
        Expense expense = Expense.builder()
                .group(group)
                .amount(request.getAmount())
                .description(request.getDescription())
                .createdBy(paidBy)
                .createdAt(LocalDateTime.now())
                .build();

        expenseRepository.save(expense);

        // 2. Save Splits
        double splitAmount = request.getAmount() / splitAmong.size();

        for (User user : splitAmong) {
            Split split = Split.builder()
                    .expense(expense)
                    .user(user)
                    .amount(splitAmount)
                    .build();

            splitRepository.save(split);

            // 3. Update balance if user paidBy
            if (!user.getId().equals(paidBy.getId())) {
                updateBalance(user, paidBy, splitAmount, group);
            }
        }
        return expense;
    }

    public void updateBalance(User fromUser, User toUser, double amount, Group group) {
        Balance existing = balanceRepository.findByFromUserAndToUserAndGroup(fromUser, toUser, group)
                .orElse(null);

        if (existing != null) {
            existing.setAmount(existing.getAmount() + amount);
            balanceRepository.save(existing);
        } else {
            // check reverse case
            Balance reverse = balanceRepository.findByFromUserAndToUserAndGroup(toUser, fromUser, group)
                    .orElse(null);

            if (reverse != null) {
                if (reverse.getAmount() > amount) {
                    reverse.setAmount(reverse.getAmount() - amount);
                    balanceRepository.save(reverse);
                } else if (reverse.getAmount() < amount) {
                    balanceRepository.save(Balance.builder()
                            .fromUser(fromUser)
                            .toUser(toUser)
                            .amount(amount-reverse.getAmount())
                            .group(group)
                            .build()
                    );
                } else {
                    balanceRepository.delete(reverse);
                }
            } else {
                // no previous balance at all
                balanceRepository.save(Balance.builder()
                        .fromUser(fromUser)
                        .toUser(toUser)
                        .amount(amount)
                        .group(group)
                        .build()
                );
            }

        }
    }

    public List<ExpenseResponse> getExpensesByGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group not found", HttpStatus.NOT_FOUND));

        List<Expense> expenses = expenseRepository.findByGroup(group);

        return expenses.stream()
                .map(expense -> {
                    List<ExpenseResponse.SplitInfo> splits = splitRepository.findByExpense(expense).stream()
                            .map((split -> ExpenseResponse.SplitInfo.builder()
                                    .user(split.getUser().getName())
                                    .amount(split.getAmount())
                                    .build())
                            ).toList();

                    return ExpenseResponse.builder()
                            .id(expense.getId())
                            .description(expense.getDescription())
                            .amount(expense.getAmount())
                            .paidBy(expense.getCreatedBy().getName())
                            .createdAt(expense.getCreatedAt())
                            .splits(splits)
                            .build();
                }).toList();
    }
}
