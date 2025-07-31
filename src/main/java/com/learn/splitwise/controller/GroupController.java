package com.learn.splitwise.controller;

import com.learn.splitwise.dto.*;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.service.ExpenseService;
import com.learn.splitwise.service.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Group")
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    private final GroupService groupService;
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest request) {
        Group group = groupService.createGroup(request);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpense(@PathVariable Long groupId) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{groupId}/dashboard")
    public ResponseEntity<GroupDashboardResponse> getGroupDashboard(@PathVariable Long groupId) {
        GroupDashboardResponse res = groupService.getGroupDashboard(groupId);
        return ResponseEntity.ok(res);
    }

    @PutMapping("{groupId}")
    public ResponseEntity<UpdateGroupResponse> updateGroup(@PathVariable Long groupId, @RequestBody UpdateGroupRequest request) {
        UpdateGroupResponse response = groupService.updateGroup(groupId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok("Group Deleted Successfully");
    }
}
