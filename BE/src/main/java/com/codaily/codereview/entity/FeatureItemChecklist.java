package com.codaily.codereview.entity;

import com.codaily.project.entity.FeatureItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feature_item_checklist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeatureItemChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checklistId;

    // 기능 연결 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureItem featureItem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String item;

    @Column(nullable = false)
    private boolean done = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "featureItemChecklist", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CodeReviewItem> codeReviewItems;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "feature_item_checklist_filepaths", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "file_path")
    private List<String> filePaths = new ArrayList<>();

    @Builder
    public FeatureItemChecklist(FeatureItem featureItem, String item, String description) {
        this.featureItem = featureItem;
        this.item = item;
        this.description = description;
    }

    public void updateItem(String item, String description) {
        this.item = item;
        this.description = description;
    }

    // 파일 경로 추가
    public void updateFilePaths(List<String> newPaths) {
        for (String path : newPaths) {
            if (!filePaths.contains(path)) {
                filePaths.add(path);
            }
        }
    }


    public void updateDone(boolean isDone) {
        this.done = isDone;
    }
}
