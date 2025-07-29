package com.learn.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UserDashboardResponse {
    private String userName;
    private Double totalBalance;
    private List<GroupBalance> groupBalances;

    @Data
    @AllArgsConstructor
    @Builder
    public static class GroupBalance {
        private Long groupId;
        private String groupName;
        private Double balance;
    }
}
