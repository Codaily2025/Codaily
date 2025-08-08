package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class SeverityByCategoryResponseDto {
    private Map<String, Map<String, Integer>> categorySeverityCount;
}
