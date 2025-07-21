package com.learn.splitwise.service;

import com.learn.splitwise.dto.CreateGroupRequest;
import com.learn.splitwise.exception.CustomException;
import com.learn.splitwise.model.Group;
import com.learn.splitwise.model.User;
import com.learn.splitwise.repository.GroupRepository;
import com.learn.splitwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public Group createGroup(CreateGroupRequest request) {

        List<User> members = userRepository.findAllById(request.getMemberIds());

        List<Group> existingGroups = groupRepository.findByName(request.getName());

        for (Group grp : existingGroups) {
            List<Long> existingUserIds = grp.getMembers().stream()
                    .map(User::getId)
                    .sorted()
                    .toList();

            List<Long> requestUserIds = request.getMemberIds().stream()
                    .sorted()
                    .toList();

            if (existingUserIds.equals(requestUserIds)) {
                throw new CustomException("A group with the same name and members already exists", HttpStatus.CONFLICT);
            }
        }

        if (members.size() != request.getMemberIds().size()) {
            throw new CustomException("One or more user IDs are invalid", HttpStatus.BAD_REQUEST);
        }

        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .members(members)
                .build();
        return groupRepository.save(group);
    }
}
