package com.codaily.retrospective.entity;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.service.RetrospectiveTriggerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String summaryJson; // Map<String, Object>로 변환 가능

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RetrospectiveTriggerType triggerType; // "AUTO" | "MANUAL"

    @Column(columnDefinition = "TEXT")
    private String userComment;
}

