package com.codaily.project.entity;

import com.codaily.project.entity.Task;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private Double qualityScore;
    private LocalDateTime createdAt;
}
