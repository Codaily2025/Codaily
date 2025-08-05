package com.codaily.common.gpt.handler;

import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.service.FeatureItemService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SpecMessageHandler implements SseMessageHandler {

    private final FeatureItemService featureItemService;
    private final ObjectMapper objectMapper;

    @Override
    public String getType() {
        return "spec";
    }

    @Override
    public void handle(JsonNode content, Long projectId, Long specId) {
        try {
            FeatureSaveRequest request = objectMapper.treeToValue(content, FeatureSaveRequest.class);
            featureItemService.saveSpecChunk(request, projectId, specId);
        } catch (Exception e) {
            log.error("명세서 저장 실패", e);
        }
    }
}
