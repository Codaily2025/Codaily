package com.codaily.codereview.entity;

import com.codaily.project.entity.FeatureItem;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "code_review_item")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CodeReviewItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column
    private String category;

    @Column
    private String filePath;

    @Column
    private String lineRange;

    @Column
    private String severity;

    @Column
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private FeatureItemChecklist featureItemChecklist;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private FeatureItem featureItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private CodeReview codeReview;


    public static CodeReviewItem of(FeatureItemChecklist featureItemChecklist,
                                    FeatureItem featureItem,
                                    String category,
                                    String filePath,
                                    String lineRange,
                                    String severity,
                                    String message) {
        return CodeReviewItem.builder()
                .featureItemChecklist(featureItemChecklist)
                .featureItem(featureItem)
                .category(category)
                .filePath(filePath)
                .lineRange(lineRange)
                .severity(severity)
                .message(message)
                .build();
    }

    public void addCodeReview(CodeReview codeReview) {
        this.codeReview = codeReview;
    }
}
