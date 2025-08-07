package com.codaily.codereview.entity;

import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_reviews")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureItem featureItem;

    @Column(name = "quality_score")
    private Double qualityScore;

    @Lob
    @Column(nullable = false)
    private String summary;

    @Lob
    @Column(name = "convention")
    private String convention;

    @Lob
    @Column(name = "refactor_suggestion")
    private String refactorSuggestion;

    @Lob
    @Column(name = "complexity")
    private String complexity;

    @Lob
    @Column(name = "bug_risk")
    private String bugRisk;

    @Lob
    @Column(name = "security_risk")
    private String securityRisk;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

