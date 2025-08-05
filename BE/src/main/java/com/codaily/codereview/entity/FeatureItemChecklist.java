package com.codaily.codereview.entity;

import com.codaily.project.entity.FeatureItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feature_item_checklist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeatureItemChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 기능 연결 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private FeatureItem featureItem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String item;

    @Column(nullable = false)
    private boolean done = false;

    @Column(columnDefinition = "TEXT")
    private String description;

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

    public void updateDone(boolean isDone) {
        this.done = isDone;
    }
}
