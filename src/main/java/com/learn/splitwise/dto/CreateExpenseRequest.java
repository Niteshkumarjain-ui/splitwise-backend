package com.learn.splitwise.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateExpenseRequest {

    private Long groupId;
    private String description;
    private Double amount;
    private Long paidByUserId;
    private List<Long> splitAmongUserIds;
}
