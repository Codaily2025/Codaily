package com.codaily.project.entity;

import jakarta.persistence.*;
import lombok.*;
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
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String complexity;         // 복잡도 분석
    private String bugRisk;            // 버그 가능성
    private String securityRisk;       // 보안 위험도
    private String refactorSuggestion; // 리팩토링 제안

    private Double qualityScore;       // 코드 품질 점수 (0 ~ 100)

    private LocalDateTime createdAt;
}
