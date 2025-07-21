package com.learn.splitwise.service;

import com.learn.splitwise.dto.GroupBalanceResponse;
import com.learn.splitwise.dto.SettleUpRequest;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.Balance;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.model.User;
import com.learn.splitwise.repository.BalanceRepository;
import com.learn.splitwise.repository.GroupRepository;
import com.learn.splitwise.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final GroupRepository groupRepository;
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;

    public List<GroupBalanceResponse> getGroupBalance(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group Not Found", HttpStatus.NOT_FOUND));

        List<Balance> balances = balanceRepository.findALlByGroup(group);

        return balances.stream()
                .map(bal -> new GroupBalanceResponse(
                        bal.getFromUser().getName(),
                        bal.getToUser().getName(),
                        bal.getAmount()
                        )
                        ).collect(Collectors.toList());

    }

    public void settleUp(Long groupId, SettleUpRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new CustomException("Group Not Found", HttpStatus.NOT_FOUND));

        User fromUser = userRepository.findById(request.getFromUserId())
                .orElseThrow(() -> new CustomException("From user not found", HttpStatus.NOT_FOUND));

        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new CustomException("To user not found", HttpStatus.NOT_FOUND));

        Balance balance = balanceRepository.findByFromUserAndToUserAndGroup(fromUser, toUser, group)
                .orElse(null);

        if (balance == null) {
            throw new CustomException("No outstanding balance from this user to settle", HttpStatus.BAD_REQUEST);
        }

        double currentAmount = balance.getAmount();
        double settleAmount = request.getAmount();
        if (settleAmount <= 0) {
            throw new CustomException("Settlement amount must be positive", HttpStatus.BAD_REQUEST);
        }

        if (settleAmount > currentAmount) {
            throw new CustomException("Settlement amount exceeds outstanding balance", HttpStatus.BAD_REQUEST);
        }

        if (settleAmount == currentAmount) {
            balanceRepository.delete(balance); // Fully settled
        } else {
            balance.setAmount(currentAmount - settleAmount); // Partially settled
            balanceRepository.save(balance);
        }

    }

}
