package com.learn.splitwise.service;

import com.learn.splitwise.dto.UpdateUserRequest;
import com.learn.splitwise.dto.UserDashboardResponse;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.model.User;
import com.learn.splitwise.repository.BalanceRepository;
import com.learn.splitwise.repository.GroupRepository;
import com.learn.splitwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final BalanceRepository balanceRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));


        List<Group> groups = groupRepository.findGroupsByUserId(user.getId());

        List<UserDashboardResponse.GroupBalance> groupBalances = new ArrayList<>();
        Double totalBalance = 0.0;

        for (Group group : groups) {
            Double positive = balanceRepository.getTotalAmountToUserInGroup(user, group);
            Double negative = balanceRepository.getTotalAmountFromUserInGroup(user, group);

            Double net = positive - negative;
            totalBalance += net;

            groupBalances.add(UserDashboardResponse.GroupBalance.builder()
                    .groupId(group.getId())
                    .groupName(group.getName())
                    .balance(net)
                    .build()
            );
        }

        return UserDashboardResponse.builder()
                .userName(user.getName())
                .totalBalance(totalBalance)
                .groupBalances(groupBalances)
                .build();
    }

    public User updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User Not found", HttpStatus.NOT_FOUND));

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        if(request.getEmail() != null && !request.getEmail().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if(request.getPassword() != null && !request.getPassword().isEmpty()) {
            // Encrypt password using BCrypt
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return User.builder()
                .id(userId)
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .createdAt(user.getCreatedAt())
                .build();

    }
}
