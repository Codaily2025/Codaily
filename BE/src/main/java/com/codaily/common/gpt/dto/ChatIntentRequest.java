package com.codaily.common.gpt.dto;

import com.codaily.project.dto.FeatureClassifyRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatIntentRequest {
    private String message;
    private List<FeatureClassifyRequest> mainFeatures;
}
