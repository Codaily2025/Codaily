package com.codaily.retrospective.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RetrospectiveGenerateResponse {
    // 메타
    private LocalDate date;
    private Long projectId;
    private Long userId;
    private String triggerType;

    // GPT가 생성한 최종 회고 본문 (Markdown 권장: 바로 렌더/저장 용이)
    private String contentMarkdown;

    // 요약 블록(대시보드 카드용)
    private RetrospectiveSummary summary;               // 하단 클래스

    // 실행 항목(내일 계획/액션아이템)
    private List<RetrospectiveActionItem> actionItems;  // 하단 클래스

    private RetrospectiveProductivityMetrics productivityMetrics;
    private List<RetrospectiveFeatureSummary> completedFeatures;
}
