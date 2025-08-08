package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CodeReviewItemResponseDto {
    private String category;
    private String filePath;
    private String lineRange;
    private String severity;
    private String message;
}
