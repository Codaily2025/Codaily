package com.codaily.retrospective.dto;

import lombok.Data;

@Data
public class RetrospectiveSummary {
    private String overall;     // 한 문단 요약
    private String strengths;   // 오늘 잘한 점(핵심 불릿 요약 문자열 or Markdown)
    private String improvements;// 개선점 요약
    private String risks;       // 리스크/주의 포인트
}
