package com.learn.splitwise.service;

import com.learn.splitwise.dto.GroupBalanceResponse;
import com.learn.splitwise.dto.NetBalanceResponse;
import com.learn.splitwise.dto.SettleUpRequest;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.Balance;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.model.User;
import com.learn.splitwise.repository.BalanceRepository;
import com.learn.splitwise.repository.GroupRepository;
import com.learn.splitwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final GroupRepository groupRepository;
    private final BalanceRepository balanceRepository;
    private final UserRepository userRepository;

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

    public List<NetBalanceResponse> getNetBalanceForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        Map<Long, Double> netMap = new HashMap<>(); // Key: other user ID, Value: net amount
        // 1. you are creditor
        List<Balance> toYou = balanceRepository.findAllByToUser(user);

        for (Balance bal: toYou) {
            netMap.put(
                    bal.getFromUser().getId(),
                    netMap.getOrDefault(bal.getFromUser().getId(), 0.0) + bal.getAmount()
            );
        }

        // 2.you are the debtor
        List<Balance> youOwe = balanceRepository.findAllByFromUser(user);

        for (Balance bal: youOwe) {
            netMap.put(
                    bal.getToUser().getId(),
                    netMap.getOrDefault(bal.getToUser().getId(), 0.0) - bal.getAmount()
            );
        }

        //3. Build response

        List<NetBalanceResponse> results = new ArrayList<>();

        for (Map.Entry<Long, Double> entry: netMap.entrySet()) {
            if (Math.abs(entry.getValue()) > 0.001) { // ignore zeroes
                User other = userRepository.findById(entry.getKey())
                        .orElseThrow(() -> new CustomException("User mismatch", HttpStatus.INTERNAL_SERVER_ERROR));

                results.add( new NetBalanceResponse(
                        other.getName(),
                        entry.getValue()
                ));
            }
        }

        return results;

    }

}
