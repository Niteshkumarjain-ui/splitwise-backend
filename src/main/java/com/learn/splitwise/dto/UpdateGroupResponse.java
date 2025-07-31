package com.learn.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateGroupResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<Members> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Members {
        private Long userId;
        private String name;
    }
}
