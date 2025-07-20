package com.learn.splitwise.model;

import com.learn.splitwise.repository.UserRepository;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user who ows the money
    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    // user who is owed the money
    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;
}
