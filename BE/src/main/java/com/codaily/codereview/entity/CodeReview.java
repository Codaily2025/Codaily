package com.codaily.codereview.entity;

import com.codaily.project.entity.FeatureItem;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.scheduling.config.Task;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private FeatureItem featureItem;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String complexity;         // 복잡도 분석
    private String bugRisk;            // 버그 가능성
    private String securityRisk;       // 보안 위험도
    private String refactorSuggestion; // 리팩토링 제안

    private Double qualityScore;       // 코드 품질 점수 (0 ~ 100)

    private LocalDateTime createdAt;
}