package com.learn.splitwise.service;

import com.learn.splitwise.dto.*;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.*;
import com.learn.splitwise.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final ExpenseRepository expenseRepository;
    private final SplitRepository splitRepository;

    public Group createGroup(CreateGroupRequest request) {

        List<User> members = userRepository.findAllById(request.getMemberIds());

        List<Group> existingGroups = groupRepository.findByName(request.getName());

        for (Group grp : existingGroups) {
            List<Long> existingUserIds = grp.getMembers().stream()
                    .map(User::getId)
                    .sorted()
                    .toList();

            List<Long> requestUserIds = request.getMemberIds().stream()
                    .sorted()
                    .toList();

            if (existingUserIds.equals(requestUserIds)) {
                throw new CustomException("A group with the same name and members already exists", HttpStatus.CONFLICT);
            }
        }

        if (members.size() != request.getMemberIds().size()) {
            throw new CustomException("One or more user IDs are invalid", HttpStatus.BAD_REQUEST);
        }

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .members(members)
                .build();
        return groupRepository.save(group);
    }

    public GroupDashboardResponse getGroupDashboard(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group not found", HttpStatus.NOT_FOUND));

        // fetch balance
        List<Balance> balances = balanceRepository.findALlByGroup(group);
        Map<Long, String> userIdToName = new HashMap<>();
        List<GroupDashboardResponse.Members> members = new ArrayList<>();
        for (User user : group.getMembers()) {
            members.add(GroupDashboardResponse.Members.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .emailId(user.getEmail())
                    .build());
        }

        List<GroupDashboardResponse.Balances> balanceDetails = new ArrayList<>();

        for (Balance balance : balances) {
            balanceDetails.add(GroupDashboardResponse.Balances.builder()
                    .fromUserId(balance.getFromUser().getId())
                    .toUserId(balance.getToUser().getId())
                    .amount(balance.getAmount())
                    .build());
        }
        List<GroupDashboardResponse.NetBalances> netBalancesList = computeNetBalances(balances);
        List<Expense> expenses = expenseRepository.findByGroup(group);

        List<GroupDashboardResponse.ExpenseHistory> expenseHistories = expenses.stream()
                .map(exp -> {
                    List<Split> splits = splitRepository.findByExpense(exp);
                    List<Long> splitUserIds = splits.stream()
                            .map(split -> split.getUser().getId())
                            .toList();

                    return GroupDashboardResponse.ExpenseHistory.builder()
                            .id(exp.getId())
                            .amount(exp.getAmount())
                            .description(exp.getDescription())
                            .createdAt(exp.getCreatedAt())
                            .createdByName(exp.getCreatedBy().getName())
                            .paidByUserId(exp.getCreatedBy().getId())
                            .splitAmongUserIds(splitUserIds)
                            .build();
                })
                .toList();


        return GroupDashboardResponse.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .description(group.getDescription())
                .members(members)
                .balances(balanceDetails)
                .netBalances(netBalancesList)
                .expenseHistory(expenseHistories)
                .build();
    }

    public List<GroupDashboardResponse.NetBalances> computeNetBalances(List<Balance> balances) {
        Map<Long, Double> netMap = new HashMap<>();

        // Step 1: Compute net balances
        for (Balance b : balances) {
            netMap.put(b.getFromUser().getId(), netMap.getOrDefault(b.getFromUser().getId(), 0.0) - b.getAmount());
            netMap.put(b.getToUser().getId(), netMap.getOrDefault(b.getToUser().getId(), 0.0) + b.getAmount());
        }

        // Step 2: Create creditors & debtors lists
        List<GroupDashboardResponse.NetBalances> creditors = new ArrayList<>();
        List<GroupDashboardResponse.NetBalances> debtors = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : netMap.entrySet()) {
            double amount = entry.getValue();
            GroupDashboardResponse.NetBalances nb = GroupDashboardResponse.NetBalances.builder()
                    .userId(entry.getKey())
                    .netBalance(amount) // preserve original net balance
                    .build();

            if (amount > 0.0001)
                creditors.add(nb);
            else if (amount < -0.0001)
                debtors.add(nb);
        }

        // Step 3: Distribute debts (using a temp balance map to avoid losing original
        // net balance)
        Map<Long, Double> tempCredits = new HashMap<>();
        for (GroupDashboardResponse.NetBalances creditor : creditors) {
            tempCredits.put(creditor.getUserId(), creditor.getNetBalance());
        }

        for (GroupDashboardResponse.NetBalances debtor : debtors) {
            double debt = -debtor.getNetBalance(); // convert to positive
            List<GroupDashboardResponse.NetBalances.UserShare> payToList = new ArrayList<>();

            for (GroupDashboardResponse.NetBalances creditor : creditors) {
                if (debt < 0.0001)
                    break;

                double creditAvailable = tempCredits.getOrDefault(creditor.getUserId(), 0.0);
                if (creditAvailable < 0.0001)
                    continue;

                double paid = Math.min(creditAvailable, debt);

                // Record payment
                payToList.add(GroupDashboardResponse.NetBalances.UserShare.builder()
                        .userId(creditor.getUserId())
                        .amount(paid)
                        .build());

                // Reduce temp balances
                tempCredits.put(creditor.getUserId(), creditAvailable - paid);
                debt -= paid;
            }

            debtor.setShouldPayTo(payToList);
        }

        // Step 4: Compute shouldReceiveFrom from debtors' shouldPayTo
        for (GroupDashboardResponse.NetBalances creditor : creditors) {
            List<GroupDashboardResponse.NetBalances.UserShare> receiveFromList = new ArrayList<>();

            for (GroupDashboardResponse.NetBalances debtor : debtors) {
                if (debtor.getShouldPayTo() == null)
                    continue;

                for (GroupDashboardResponse.NetBalances.UserShare share : debtor.getShouldPayTo()) {
                    if (share.getUserId().equals(creditor.getUserId())) {
                        receiveFromList.add(GroupDashboardResponse.NetBalances.UserShare.builder()
                                .userId(debtor.getUserId())
                                .amount(share.getAmount())
                                .build());
                    }
                }
            }

            creditor.setShouldReceiveFrom(receiveFromList);
            // ✅ netBalance stays as original positive value
        }

        // Step 5: Combine debtors and creditors into one list
        List<GroupDashboardResponse.NetBalances> all = new ArrayList<>();
        all.addAll(debtors);
        all.addAll(creditors);

        return all;
    }

    public UpdateGroupResponse updateGroup(Long groupId, UpdateGroupRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group Not found", HttpStatus.NOT_FOUND));

        if (request.getName() != null && !request.getName().isEmpty()) {
            group.setName(request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            group.setDescription(request.getDescription());
        }

        // Update members if provided
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(request.getMemberIds());
            group.setMembers(members); // replaces old members
        }

        groupRepository.save(group);

        return UpdateGroupResponse.builder()
                .id(groupId)
                .description(group.getDescription())
                .name(group.getName())
                .createdAt(group.getCreatedAt())
                .members(group.getMembers()
                        .stream()
                        .map(user -> UpdateGroupResponse.Members.builder()
                                .userId(user.getId())
                                .name(user.getName())
                                .build())
                        .toList())
                .build();
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group Not found", HttpStatus.NOT_FOUND));

        // 1. Delete Splits of all Expenses in this group
        List<Expense> expenses = expenseRepository.findByGroup(group);
        for (Expense expense : expenses) {
            splitRepository.deleteByExpense(expense);
        }

        // 2. Delete Expenses
        expenseRepository.deleteAll(expenses);

        // 3. Delete Balances
        balanceRepository.deleteByGroup(group);
        group.getMembers().clear();
        groupRepository.save(group);
        groupRepository.delete(group);
    }
}
