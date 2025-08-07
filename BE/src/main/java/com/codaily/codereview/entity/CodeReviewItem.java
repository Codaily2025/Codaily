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
    private Long id;

    @Column(nullable = false)
    private String category;           // 예: 보안 위험, 성능 최적화 등

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String lineRange;

    @Column(nullable = false)
    private String severity;

    @Lob
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", nullable = false)
    private FeatureItemChecklist featureItemChecklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private FeatureItem featureItem;


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
}
