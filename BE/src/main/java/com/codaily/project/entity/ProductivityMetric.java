package com.codaily.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "productivity_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductivityMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private LocalDate date;

    // 기본 메트릭
    private int completedTasks;
    private int commits;
    private double productivityScore;

    // 개별 점수
    private double commitFrequencyScore;
    private double taskCompletionScore;
    private double codeQualityScore;

    // 가중치
    private double commitWeight;
    private double taskWeight;
    private double codeQualityWeight;

    @Enumerated(EnumType.STRING)
    private TrendType trend;

    private double personalAverage;
    private double projectAverage;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum TrendType {
        IMPROVING, DECLINING, STABLE
    }
}