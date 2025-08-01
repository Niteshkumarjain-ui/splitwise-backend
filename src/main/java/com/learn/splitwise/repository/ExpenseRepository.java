package com.learn.splitwise.repository;

import com.learn.splitwise.model.Expense;
import com.learn.splitwise.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroup(Group group);
    void deleteByGroup(Group group);
}
