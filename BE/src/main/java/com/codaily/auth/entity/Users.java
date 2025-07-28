package com.codaily.auth.entity;

import com.codaily.project.entity.Projects;
import com.codaily.project.entity.Specifications;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;
import java.util.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String socialProvider;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 255)
    private String githubAccount;

    @Column(columnDefinition = "TEXT")
    private String githubProfileUrl;

    @Column(columnDefinition = "TEXT")
    private String githubAccessToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Projects> projects;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Specifications> specifications;

    public enum Role {
        USER, ADMIN
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (role == null) {
            role = Role.USER;
        }
    }
}
