package com.codaily.auth.entity;

import com.codaily.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String socialProvider;

    @Column(nullable = true, length = 255)
    private String email;

    @Column(length = 255)
    private String socialId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 255)
    private String password;

    private LocalDateTime tokenExpiredAt;

    @Column(length = 255, name = "github_account")
    private String githubAccount;

    @Column(columnDefinition = "TEXT", name = "github_access_token")
    private String githubAccessToken;

    @Column(columnDefinition = "TEXT", name = "github_profile_url")
    private String githubProfileUrl;

    @Column(columnDefinition = "TEXT", name = "github_scope")
    private String githubScope;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Project> projects;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TechStack> techStacks = new ArrayList<>();

    // ====== [소셜 로그인 공통 토큰 저장 필드] ======
    @Column(columnDefinition = "TEXT")
    private String socialAccessToken;

    @Column(columnDefinition = "TEXT")
    private String socialRefreshToken;

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

    // 작성자: yeongenn - 최초 로그인인지 판별하는 메서드 (추가 정보 입력이 필요한지 확인)
    @Transient
    public boolean isFirstLogin() {
        // githubAccount가 null이거나 비어있는경우 최초 로그인으로 판단
        boolean hasNoGithubAccount = this.githubAccount == null || this.githubAccount.trim().isEmpty();

        return hasNoGithubAccount;
    }
}
