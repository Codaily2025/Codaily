package com.codaily.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_productivity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_id", "date"}))
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
        this.createdAt = LocalDateTime.now();
    }

    @Column(nullable = true)
    private Integer completedFeatures;

    @Column(nullable = true)
    private Integer totalCommits;

    // 서비스에서 NULL 체크
    public int getCompletedFeaturesOrZero() {
        return completedFeatures != null ? completedFeatures : 0;
    }


}