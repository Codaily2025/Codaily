package com.codaily.management.entity;

import com.codaily.project.entity.FeatureItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "feature_item_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureItemSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureItem featureItem;

    @Column(nullable = false)
    private LocalDate scheduleDate;

    @Column(nullable = false)
    private Double allocatedHours;

    @Column(nullable = false)
    private Boolean withinProjectPeriod;
}
