package com.codaily.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

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
    private String password;

    @Column(length = 255)
    private String githubAccount;

    @Column(columnDefinition = "TEXT")
    private String githubProfileUrl;

    @Column(columnDefinition = "TEXT")
    private String githubAccessToken;

    private LocalDateTime tokenExpiredAt;

    @Column(columnDefinition = "TEXT")
    private String githubScope;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = Role.USER;
        }
    }

    public enum Role {
        USER, ADMIN
    }
}