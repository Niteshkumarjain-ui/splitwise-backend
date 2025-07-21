package com.learn.splitwise.repository;

import com.learn.splitwise.model.Balance;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findByFromUserAndToUserAndGroup(User fromUser, User toUser, Group group);
    List<Balance> findALlByGroup(Group group);
}
