package com.codaily.codereview.controller;

import com.codaily.codereview.dto.CodeReviewItemResponseDto;
import com.codaily.codereview.dto.CodeReviewSummaryResponseDto;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.service.FeatureItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.HashMap;
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

        Map<String, String> summaryMap = new HashMap<>();
        summaryMap.put("convention", summaryDto.getConvention());
        summaryMap.put("performance", summaryDto.getPerformance());
        summaryMap.put("security", summaryDto.getSecurityRisk());
        summaryMap.put("complexity", summaryDto.getComplexity());
        summaryMap.put("bugRisk", summaryDto.getBugRisk());
        summaryMap.put("refactoring", summaryDto.getRefactorSuggestion());


        Map<String, Object> codeReviewSections = new LinkedHashMap<>();

        for (String category : summaryMap.keySet()) {
            List<Map<String, String>> issues = groupedByCategory
                    .getOrDefault(category, List.of())
                    .stream()
                    .map(item -> {
                        Map<String, String> issueMap = new HashMap<>();
                        issueMap.put("file", item.getFilePath());
                        issueMap.put("line", item.getLineRange());
                        issueMap.put("description", item.getMessage());
                        issueMap.put("level", item.getSeverity());
                        return issueMap;
                    })
                    .toList();

            Map<String, Object> sectionMap = new HashMap<>();
            sectionMap.put("summary", summaryMap.get(category)); // null 허용됨
            sectionMap.put("issues", issues);

            codeReviewSections.put(category, sectionMap);
        }


        response.put("codeReview", codeReviewSections);
        return response;
    }
}

