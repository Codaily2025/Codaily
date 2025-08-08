package com.codaily.codereview.entity;

import com.codaily.project.entity.FeatureItem;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.scheduling.config.Task;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_commits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeCommit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private FeatureItem featureItem;

    private String commitHash;
    private String author;
    private String message;
    private LocalDateTime committedAt;
}