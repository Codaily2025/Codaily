package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewRunRequestDto {
    private Long projectId;
    private Long commitId;
    private String commitHash;
    private Long featureId; // ✅ 기능명 유추된 후에 세팅 (or null)
    private List<DiffFile> diffFiles;
}

