package com.learn.splitwise.repository;

import com.learn.splitwise.model.Balance;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findByFromUserAndToUserAndGroup(User fromUser, User toUser, Group group);
    List<Balance> findALlByGroup(Group group);
    List<Balance> findAllByFromUser(User fromUser);
    List<Balance> findAllByToUser(User toUser);
    void deleteByGroup(Group group);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Balance b WHERE b.toUser = :user AND b.group = :group")
    Double getTotalAmountToUserInGroup(@Param("user") User user, @Param("group") Group group);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Balance b WHERE b.fromUser = :user AND b.group = :group")
    Double getTotalAmountFromUserInGroup(@Param("user") User user, @Param("group") Group group);
}
