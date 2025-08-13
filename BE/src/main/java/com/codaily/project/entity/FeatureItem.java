package com.codaily.project.entity;

import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.entity.CodeReview;
import com.codaily.management.entity.FeatureItemSchedule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "feature_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FeatureItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long featureId;
    //기능이름
    @Column(nullable = false, columnDefinition="TEXT")
    private String title;

    //기능설명
    @Column(columnDefinition = "TEXT")
    private String description;

    //대주제
    @Column(length=50)
    private String field;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isSelected = true;
    private Boolean isCustom = false;
    private Integer priorityLevel;

    @Column(length=50)
    private String category;

    @Column(nullable = false, length=50)
    @Builder.Default
    private String status = "TODO";

    @Builder.Default
    @Column(nullable = false)
    private Boolean isReduced = false;
    private Double estimatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "featureItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CodeReview> codeReviews;

    @OneToMany(mappedBy = "featureItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CodeCommit> codeCommits;

    @JsonProperty("projectId")
    public Long getProjectId(){
        return project != null ? project.getProjectId() : null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spec_id")
    @JsonIgnore
    private Specification specification;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_feature_id")
    @JsonIgnore
    private FeatureItem parentFeature;

    @OneToMany(mappedBy = "parentFeature", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("priorityLevel ASC, estimatedTime ASC, featureId ASC")
    @JsonIgnore
    private List<FeatureItem> childFeatures = new ArrayList<>();

    @OneToMany(mappedBy = "featureItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FeatureItemSchedule> schedules = new ArrayList<>();

    @Column(name = "remaining_time")
    private Double remainingTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        // 상태가 DONE으로 변경될 때만 completedAt 설정
        if ("DONE".equals(this.status) && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

}
