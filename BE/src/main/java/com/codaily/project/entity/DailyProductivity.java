package com.codaily.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_productivity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyProductivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dailyId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private LocalDate date;

    private int completedTasks;
    private int commits;
    private double codeQuality;
    private double productivityScore;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}