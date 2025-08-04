package com.learn.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class GroupDashboardResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private List<Members> members;
    private List<Balances> balances;
    private List<NetBalances> netBalances;
    private List<ExpenseHistory> expenseHistory;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Members {
        private Long id;
        private String name;
        private String emailId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class Balances {
        private Long toUserId;
        private Long fromUserId;
        private Double amount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ExpenseHistory {
        private Long id;
        private String description;
        private Double amount;
        private LocalDateTime createdAt;
        private String createdByName;
        private Long paidByUserId;
        private List<Long> splitAmongUserIds;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class NetBalances {
        private Long userId;
        private Double netBalance;
        private List<UserShare> shouldPayTo;
        private List<UserShare> shouldReceiveFrom;

        @Data
        @Builder
        @AllArgsConstructor
        public static class UserShare {
            private Long userId;
            private Double amount;
        }
    }
}
