package com.learn.splitwise.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateExpenseRequest {
    private String description;
    private Double amount;
    private List<Long> splitAmongUserIds;
}
