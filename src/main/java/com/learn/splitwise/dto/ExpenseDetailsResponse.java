package com.learn.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ExpenseDetailsResponse {
    private Long id;
    private String description;
    private Double amount;
    private String paidBy;
    private LocalDateTime createdAt;
    private List<Split> splits;

    @Data
    @AllArgsConstructor
    @Builder
    public static class Split {
        private String userName;
        private Double amountOwed;
    }
}
