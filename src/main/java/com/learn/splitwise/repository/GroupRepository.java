package com.learn.splitwise.repository;

import com.learn.splitwise.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
