package com.codaily.codereview.entity;

import com.codaily.project.entity.Task;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_commits")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeCommit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    private String commitHash;
    private String author;
    private String message;
    private LocalDateTime committedAt;
}