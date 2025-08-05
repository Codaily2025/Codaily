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
public class FeatureReviewResultDto {
    private Long featureId;
    private String featureName;
    private List<ChecklistReviewResult> codeReviewItems;  // 기능별 리뷰 묶음

    /*
    {
          "featureId": 5,
          "featureName": "소셜 로그인",
          "codeReviewItems": [
            {
              "checklistItem": "JWT 발급",
              "summary": "...",
              "codeReviews": [...]
            },
            {
              "checklistItem": "OAuth 설정",
              "summary": "...",
              "codeReviews": [...]
            }
          ]
        }

     */

}
