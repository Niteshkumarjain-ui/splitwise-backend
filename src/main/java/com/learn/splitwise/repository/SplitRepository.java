package com.learn.splitwise.repository;

import com.learn.splitwise.model.Expense;
import com.learn.splitwise.model.Split;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SplitRepository extends JpaRepository<Split, Long> {
    List<Split> findByExpense(Expense expense);
}
