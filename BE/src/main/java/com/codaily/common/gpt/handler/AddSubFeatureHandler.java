package com.codaily.common.gpt.handler;

import com.codaily.project.dto.SubFeatureSaveResponse;
import com.codaily.project.dto.SubFeatureSaveRequest;
import com.codaily.project.service.FeatureItemService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class AddSubFeatureHandler implements SseMessageHandler<SubFeatureSaveResponse> {

    private final FeatureItemService featureItemService;
    private final ObjectMapper objectMapper;

    @Override
    public MessageType getType() {
        return MessageType.SPEC_ADD_FEATURE_SUB;
    }

    @Override
    public Class<SubFeatureSaveResponse> getResponseType() {
        return SubFeatureSaveResponse.class;
    }

    @Override
    public SubFeatureSaveResponse handle(JsonNode content, Long projectId, Long specId, Long featureId) {
        try {
            SubFeatureSaveRequest request = objectMapper.treeToValue(content, SubFeatureSaveRequest.class);
            log.info("handler saveSubFeatureChunk...");
            return featureItemService.saveSubFeatureChunk(request, projectId, specId);
        } catch (Exception e) {
            log.error("서브 기능 저장 실패", e);
            throw new RuntimeException("서브 기능 저장 실패", e);
        }
    }
}

