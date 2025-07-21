package com.learn.splitwise.dto;

import lombok.Data;

@Data
public class SettleUpRequest {
    private Long fromUserId;
    private Long toUserId;
    private Double amount;
}
