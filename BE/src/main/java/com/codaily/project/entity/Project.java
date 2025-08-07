package com.codaily.project.entity;

import com.codaily.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spec_id")
    private Specification specification;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

//    @Column(length = 20)
//    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeatureItem> featureItems;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectRepositories> projectRepositories = new ArrayList<>();

    @PrePersist
    protected void onCreated() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void updateDate() {
        updatedAt = LocalDateTime.now();
    }

    // status 값들을 enum으로 관리
    public enum ProjectStatus {
        TODO, IN_PROGRESS, COMPLETED
    }

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProjectStatus status;
}
