package com.codaily.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "daily_productivity",
uniqueConstraints = @UniqueConstraint(
        name = "uk_daily_productivity_user_project_date",
        columnNames = {"user_id", "project_id", "date"}
))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyProductivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private LocalDate date;

    private double codeQuality;
    private double productivityScore;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (completedFeatures == null) completedFeatures = 0;
        if (totalCommits == null) totalCommits = 0;
    }

    @Column(name = "completed_tasks", nullable = false)
    private Integer completedFeatures;

    @Column(name = "commits", nullable = false)
    private Integer totalCommits;

    // 서비스에서 NULL 체크
    public int getCompletedFeaturesOrZero() {
        return completedFeatures != null ? completedFeatures : 0;
    }


}