package com.codaily.common.gpt.handler;

import com.codaily.project.dto.FeatureSaveRequest;
import com.codaily.project.dto.FeatureSaveResponse;
import com.codaily.project.service.FeatureItemService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class RegenerateSpecMessageHandler implements SseMessageHandler<FeatureSaveResponse> {

    private final FeatureItemService featureItemService;
    private final ObjectMapper objectMapper;

    @Override
    public MessageType getType() {
        return MessageType.SPEC_REGENERATE;
    }

    @Override
    public Class<FeatureSaveResponse> getResponseType() {
        return FeatureSaveResponse.class;
    }

    @Override
    public FeatureSaveResponse handle(JsonNode content, Long projectId, Long specId, Long featureId) {
        try {
            FeatureSaveRequest request = objectMapper.treeToValue(content, FeatureSaveRequest.class);
            return featureItemService.saveSpecChunk(request, projectId, specId, "spec:regenerate");
        } catch (Exception e) {
            log.error("명세 재생성 실패", e);
            throw new RuntimeException("명세 재생성 실패", e);
        }
    }
}
