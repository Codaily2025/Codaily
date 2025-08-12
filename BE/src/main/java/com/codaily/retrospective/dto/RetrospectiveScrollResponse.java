package com.codaily.retrospective.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "무한 스크롤 회고 조회 응답")
public class RetrospectiveScrollResponse {

    @Schema(description = "조회된 회고 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RetrospectiveGenerateResponse> items;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지 조회용 before 커서(yyyy-MM-dd), 없으면 마지막 페이지", example = "2025-08-07")
    private String nextBefore;
}

