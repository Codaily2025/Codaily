package com.codaily.codereview.controller;

import com.codaily.codereview.dto.CodeReviewItemResponseDto;
import com.codaily.codereview.dto.CodeReviewSummaryResponseDto;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.service.FeatureItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CodeReviewResponseMapper {

    private final FeatureItemService featureItemService;

    public Map<String, Object> toDetailResponse(
            Long featureId,
            CodeReviewSummaryResponseDto summaryDto,
            List<CodeReviewItemResponseDto> items
    ) {
        FeatureItem featureItem = featureItemService.findById(featureId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("featureId", featureId);
        response.put("featureCategory", featureItem.getField());
        response.put("reviewName", featureItem.getTitle());
        response.put("reviewDate", summaryDto.getCreatedAt().toString());
        response.put("reviewScore", summaryDto.getQualityScore());

        Map<String, List<CodeReviewItemResponseDto>> groupedByCategory =
                items.stream().collect(Collectors.groupingBy(CodeReviewItemResponseDto::getCategory));

        Map<String, String> summaryMap = Map.of(
                "convention", summaryDto.getConvention(),
                "performance", summaryDto.getPerformance(),
                "security", summaryDto.getSecurityRisk(),
                "complexity", summaryDto.getComplexity(),
                "bugRisk", summaryDto.getBugRisk(),
                "refactoring", summaryDto.getRefactorSuggestion()
        );

        Map<String, Object> codeReviewSections = new LinkedHashMap<>();

        for (String category : summaryMap.keySet()) {
            List<Map<String, String>> issues = groupedByCategory
                    .getOrDefault(category, List.of())
                    .stream()
                    .map(item -> Map.of(
                            "file", item.getFilePath(),
                            "line", item.getLineRange(),
                            "description", item.getMessage(),
                            "level", item.getSeverity()
                    ))
                    .toList();

            codeReviewSections.put(category, Map.of(
                    "summary", summaryMap.get(category),
                    "issues", issues
            ));
        }

        response.put("codeReview", codeReviewSections);
        return response;
    }
}


