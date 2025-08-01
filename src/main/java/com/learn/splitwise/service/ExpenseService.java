package com.learn.splitwise.service;

import com.learn.splitwise.dto.CreateExpenseRequest;
import com.learn.splitwise.dto.ExpenseDetailsResponse;
import com.learn.splitwise.dto.ExpenseResponse;
import com.learn.splitwise.dto.UpdateExpenseRequest;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.*;
import com.learn.splitwise.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        // 1. Check existing forward balance
        Balance forward = balanceRepository.findByFromUserAndToUserAndGroup(fromUser, toUser, group).orElse(null);
        Balance reverse = balanceRepository.findByFromUserAndToUserAndGroup(toUser, fromUser, group).orElse(null);

        if (forward != null) {
            // Case 1: already owes in same direction → add amount
            forward.setAmount(forward.getAmount() + amount);
            balanceRepository.save(forward);

        } else if (reverse != null) {
            // Case 2: reverse direction exists → offset
            if (reverse.getAmount() > amount) {
                // reduce reverse balance
                reverse.setAmount(reverse.getAmount() - amount);
                balanceRepository.save(reverse);

            } else if (reverse.getAmount() < amount) {
                // delete reverse and create new forward
                double newAmount = amount - reverse.getAmount();
                balanceRepository.delete(reverse);

                balanceRepository.save(Balance.builder()
                        .fromUser(fromUser)
                        .toUser(toUser)
                        .amount(newAmount)
                        .group(group)
                        .build()
                );

            } else {
                // exact offset → delete reverse
                balanceRepository.delete(reverse);
            }

        } else {
            // Case 3: no previous balance → create new forward
            balanceRepository.save(Balance.builder()
                    .fromUser(fromUser)
                    .toUser(toUser)
                    .amount(amount)
                    .group(group)
                    .build()
            );
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

    @Transactional
    public Expense updateExpense(Long expenseId, UpdateExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new CustomException("Expense is not found", HttpStatus.NOT_FOUND));

        List<Split> oldSplits = splitRepository.findByExpense(expense);
        for (Split split : oldSplits) {
            if (!split.getUser().getId().equals(expense.getCreatedBy().getId())) {
                reverseBalance(split.getUser(), expense.getCreatedBy(), split.getAmount(), expense.getGroup());
            }
        }
        // Delete old splits
        splitRepository.deleteAll(oldSplits);

        // Update expense
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expenseRepository.save(expense);

        List<User> users = userRepository.findAllById(request.getSplitAmongUserIds());
        double splitAmount = request.getAmount() / users.size();

        for (User user : users) {
            Split newSplit = Split.builder()
                    .expense(expense)
                    .user(user)
                    .amount(splitAmount)
                    .build();
            splitRepository.save(newSplit);

            if(!user.getId().equals(expense.getCreatedBy().getId())) {
                updateBalance(user, expense.getCreatedBy(), splitAmount, expense.getGroup());
            }
        }

        return expense;
    }

    private void reverseBalance(User fromUser, User toUser, Double amount, Group group) {
        Balance balance = balanceRepository.findByFromUserAndToUserAndGroup(fromUser, toUser, group).orElse(null);

        if (balance != null) {
            if (balance.getAmount() > amount) {
                balance.setAmount(balance.getAmount() - amount);
                balanceRepository.save(balance);
            } else if (balance.getAmount().equals(amount)) {
                balanceRepository.delete(balance);
            } else {
                balanceRepository.delete(balance);
                updateBalance(toUser, fromUser, amount - balance.getAmount(), group);
            }
        } else {
            updateBalance(toUser, fromUser, amount, group);
        }
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new CustomException("Expense not found", HttpStatus.NOT_FOUND));

        // reverse all balances
        List<Split>splits = splitRepository.findByExpense(expense);

        for(Split split: splits) {
            if (!split.getUser().getId().equals(expense.getCreatedBy().getId())) {
                reverseBalance(split.getUser(), expense.getCreatedBy(), split.getAmount(), expense.getGroup());
            }
        }

        splitRepository.deleteAll(splits);
        expenseRepository.delete(expense);
    }

    public ExpenseDetailsResponse getExpenseDetails(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new CustomException("Expense not found", HttpStatus.NOT_FOUND));

        List<Split> splits = splitRepository.findByExpense(expense);

        List<ExpenseDetailsResponse.Split> splitList = new ArrayList<>();
        for (Split split: splits) {
            splitList.add( ExpenseDetailsResponse.Split.builder()
                    .userName(split.getUser().getName())
                    .amountOwed(split.getAmount())
                    .build()
            );
        }

        return ExpenseDetailsResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .paidBy(expense.getCreatedBy().getName())
                .splits(splitList)
                .amount(expense.getAmount())
                .createdAt(expense.getCreatedAt())
                .build();



    }
}
