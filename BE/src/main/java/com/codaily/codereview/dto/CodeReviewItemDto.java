package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReviewItemDto {
    private String category;
    private String filePath;
    private String lineRange;
    private String severity;
    private String message;
}
