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
public class AddMainFeatureHandler implements SseMessageHandler<FeatureSaveResponse> {

    private final FeatureItemService featureItemService;
    private final ObjectMapper objectMapper;

    @Override
    public MessageType getType() {
        return MessageType.SPEC_ADD_FEATURE_MAIN;
    }

    @Override
    public Class<FeatureSaveResponse> getResponseType() {
        return FeatureSaveResponse.class;
    }

    @Override
    public FeatureSaveResponse handle(JsonNode content, Long projectId, Long specId, Long featureId) {
        try {
            FeatureSaveRequest request = objectMapper.treeToValue(content, FeatureSaveRequest.class);
            return featureItemService.saveSpecChunk(request, projectId, specId, "spec:add:feature:main");
        } catch (Exception e) {
            log.error("주 기능 추가 실패", e);
            throw new RuntimeException("주 기능 추가 실패", e);
        }
    }
}
