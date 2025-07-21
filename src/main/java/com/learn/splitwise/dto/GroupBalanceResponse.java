package com.learn.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupBalanceResponse {
    private String fromUser;
    private String toUser;
    private Double amount;
}
