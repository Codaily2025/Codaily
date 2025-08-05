package com.codaily.management.entity;

import com.codaily.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "days_of_week")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DaysOfWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long daysId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 20)
    private String dateName;

    private String dayName;

    @Column(nullable = false)
    private Integer hours;
}
