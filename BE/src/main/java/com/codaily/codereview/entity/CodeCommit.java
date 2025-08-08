package com.codaily.codereview.entity;

import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.scheduling.config.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    private String commitHash;
    private String author;
    private String message;

    @ElementCollection
    @CollectionTable(
            name = "code_commit_feature",
            joinColumns = @JoinColumn(name = "commit_id")
    )

    @Column(name = "feature_name")
    private List<String> featureNames = new ArrayList<>();

    private LocalDateTime committedAt;

    public void addFeatureName(String featureName) {
        this.featureNames.add(featureName);
    }
}
