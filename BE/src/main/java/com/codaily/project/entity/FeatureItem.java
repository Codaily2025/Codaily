package com.codaily.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Boolean isCustom = false;
    private Integer priorityLevel;

    @Column(length=50)
    private String category;

    @Column(nullable = false, length=50)
    @Builder.Default
    private String status = "TODO";

    @Column(nullable = false)
    private Boolean isReduced = false;
    private Double estimatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @JsonProperty("projectId")
    public Long getProjectId(){
        return project != null ? project.getProjectId() : null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spec_id")
    @JsonIgnore
    private Specification specification;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_feature_id")
    @JsonIgnore
    private FeatureItem parentFeature;

    @OneToMany(mappedBy = "parentFeature", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<FeatureItem> childFeatures = new ArrayList<>();

    @Transient
    private Double remainingTime;
}
