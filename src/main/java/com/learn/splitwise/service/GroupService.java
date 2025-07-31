package com.learn.splitwise.service;

import com.learn.splitwise.dto.CreateGroupRequest;
import com.learn.splitwise.dto.GroupDashboardResponse;
import com.learn.splitwise.dto.UpdateGroupRequest;
import com.learn.splitwise.dto.UpdateGroupResponse;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.Balance;
import com.learn.splitwise.model.Expense;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.model.User;
import com.learn.splitwise.repository.BalanceRepository;
import com.learn.splitwise.repository.ExpenseRepository;
import com.learn.splitwise.repository.GroupRepository;
import com.learn.splitwise.repository.UserRepository;
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
        for (User user: group.getMembers()) {
            members.add(GroupDashboardResponse.Members.builder()
                    .userId(user.getId())
                    .userName(user.getName())
                    .build());
        }

        List<GroupDashboardResponse.Balances> balanceDetails = new ArrayList<>();

        for(Balance balance: balances) {
            balanceDetails.add(GroupDashboardResponse.Balances.builder()
                    .fromUserId(balance.getFromUser().getId())
                    .toUserId(balance.getToUser().getId())
                    .amount(balance.getAmount())
                    .build());
        }
        List<GroupDashboardResponse.NetBalances> netBalancesList = computeNetBalances(balances);
        List<Expense> expenses = expenseRepository.findByGroup(group);

        List<GroupDashboardResponse.ExpenseHistory> expenseHistories = expenses.stream()
                .map(exp -> GroupDashboardResponse.ExpenseHistory.builder()
                        .id(exp.getId())
                        .amount(exp.getAmount())
                        .description(exp.getDescription())
                        .createdAt(exp.getCreatedAt())
                        .createdByName(exp.getCreatedBy().getName())
                        .build()
                ).toList();

        return GroupDashboardResponse.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .members(members)
                .balances(balanceDetails)
                .netBalances(netBalancesList)
                .expenseHistory(expenseHistories)
                .build();
    }

    public List<GroupDashboardResponse.NetBalances> computeNetBalances(List<Balance> balances) {
        Map<Long, Double> netMap = new HashMap<>();

        for (Balance b: balances) {
            netMap.put(b.getFromUser().getId(), netMap.getOrDefault(b.getFromUser().getId(),0.0) - b.getAmount());
            netMap.put(b.getToUser().getId(), netMap.getOrDefault(b.getToUser().getId(),0.0) + b.getAmount());
        }

        // seperate creditor/debitors
        List<GroupDashboardResponse.NetBalances> creditors = new ArrayList<>();
        List<GroupDashboardResponse.NetBalances> debitors = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : netMap.entrySet()) {
            Double amount = entry.getValue();
            GroupDashboardResponse.NetBalances netBalance = GroupDashboardResponse.NetBalances.builder()
                    .userId(entry.getKey())
                    .netBalance(amount)
                    .build();
            if (amount > 0) creditors.add(netBalance);
            else if (amount < 0) debitors.add(netBalance);
        }

        // Match debitors to creaditors

        for (GroupDashboardResponse.NetBalances debtors: debitors) {
            Double debt = - debtors.getNetBalance();
            List<GroupDashboardResponse.NetBalances.UserShare> payToList = new ArrayList<>();

            for (GroupDashboardResponse.NetBalances credior: creditors) {
                if (debt == 0) break;

                Double credit = credior.getNetBalance();
                if (credit == 0) continue;

                Double paid = Math.min(credit, debt);
                payToList.add(GroupDashboardResponse.NetBalances.UserShare.builder()
                        .userId(credior.getUserId())
                        .amount(paid)
                        .build()
                );
                credior.setNetBalance(credit-paid);
                debt -= paid;
            }
            debtors.setShouldPayTo(payToList);
        }

        for (GroupDashboardResponse.NetBalances creditor: creditors) {
            Double credit = - creditor.getNetBalance();
            List<GroupDashboardResponse.NetBalances.UserShare> receiveFromList  = new ArrayList<>();

            for (GroupDashboardResponse.NetBalances debtor: debitors) {
                if (debtor.getShouldPayTo() == null) continue;

                for (GroupDashboardResponse.NetBalances.UserShare share : debtor.getShouldPayTo()) {
                    if (share.getUserId().equals(creditor.getUserId())) {
                        receiveFromList.add(GroupDashboardResponse.NetBalances.UserShare.builder()
                                .userId(debtor.getUserId())
                                .amount(share.getAmount())
                                .build()
                        );
                    }
                }


            }

            creditor.setShouldReceiveFrom(receiveFromList);
        }
        List<GroupDashboardResponse.NetBalances> all = new ArrayList<>();
        all.addAll(debitors);
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
                                .build()
                        ).toList()
                ).build();
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group Not found", HttpStatus.NOT_FOUND));

        group.getMembers().clear();
        groupRepository.save(group);
        groupRepository.delete(group);
    }
}
