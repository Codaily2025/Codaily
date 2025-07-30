package com.codaily.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "feature_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"project", "specification", "parentFeature", "childFeatures"})
public class FeatureItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long featureId;
    //기능이름
    @Column(nullable = false, columnDefinition="TEXT")
    private String title;

    //기능설명
    @Column(columnDefinition = "TEXT")
    private String description;

    //대주제
    @Column(length=50)
    private String field;

    @Column(nullable = false)
    private Boolean isSelected = true;
    private Boolean isCustom;
    private Integer priorityLevel;

    @Column(length=50)
    private String category;

    @Column(length=50)
    private String status;

    @Column(nullable = false)
    private Boolean isReduced = false;
    private Integer estimatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spec_id")
    private Specification specification;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_feature_id")
    private FeatureItem parentFeature;

    @OneToMany(mappedBy = "parentFeature", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeatureItem> childFeatures = new ArrayList<>();
}
