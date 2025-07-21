package com.learn.splitwise.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private String description;
    private Double amount;
    private String paidBy;
    private LocalDateTime createdAt;
    private List<SplitInfo> splits;

    @Data
    @Builder
    public static class SplitInfo {
        private String user;
        private Double amount;
    }

}
