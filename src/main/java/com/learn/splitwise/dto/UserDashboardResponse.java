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
        private String description;
        private List<UserInfo> memberIds;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String emailId;
    }
}
