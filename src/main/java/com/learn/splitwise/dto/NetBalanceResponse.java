package com.learn.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NetBalanceResponse {

    private String otherUser;
    private Double amount; // Positive: they owe you | Negative: you owe them
}
