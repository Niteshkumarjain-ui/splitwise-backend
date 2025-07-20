package com.learn.splitwise.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {
    private String name;
    private String description;
    private List<Long> memberIds;
}
