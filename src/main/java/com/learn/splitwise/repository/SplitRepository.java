package com.learn.splitwise.repository;

import com.learn.splitwise.model.Split;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SplitRepository extends JpaRepository<Split, Long> {
}
