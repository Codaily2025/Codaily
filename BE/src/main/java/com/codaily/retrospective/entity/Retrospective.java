package com.codaily.retrospective.entity;

import com.codaily.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "retrospectives", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Retrospective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long retroId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "jsonb")
    private String summaryJson; // Map<String, Object>로 변환 가능

    @Column(length = 20)
    private String triggerType = "AUTO"; // "AUTO" | "MANUAL"

    @Column(columnDefinition = "TEXT")
    private String userComment;
}

