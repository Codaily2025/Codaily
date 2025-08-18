package com.codaily.common.gpt.service;

import com.codaily.common.gpt.dto.ChatIntentRequest;
import com.codaily.common.gpt.dto.ChatIntentResponse;
import com.codaily.project.dto.FeatureClassifyRequest;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.service.FeatureItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final WebClient langchainWebClient;
    private final FeatureItemService featureItemService;

    public Flux<String> streamChat(String intent, Long projectId, String message, Long featureId, String field, Double time) {
        log.info("streamChat param: {}, {}, {}, {}, {}, {}",intent, projectId, message,featureId, field, time);
        return langchainWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/ai/api/chat/gpt/stream")
                            .queryParam("intent", intent)
                            .queryParam("project_id", projectId)
                            .queryParam("message", message)
                            .queryParam("field", field)
                            .queryParam("time", time);

                    if (featureId != null) {
                        uriBuilder.queryParam("feature_id", featureId);
                        String title = featureItemService.getFeature(featureId).getTitle();
                        uriBuilder.queryParam("title", title);
                    }

                    return uriBuilder.build();
                })
                .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
    }

    public Mono<ChatIntentResponse> classifyIntent(String message, Long projectId) {
        List<FeatureItem> features = featureItemService.getAllMainFeature(projectId);
        List<FeatureClassifyRequest> mainFeatures = features.stream()
                .map(f -> new FeatureClassifyRequest(f.getFeatureId(), f.getTitle(), f.getField()))
                .toList();
        return langchainWebClient.post()
                .uri("/ai/api/chat/intent")
                .bodyValue(new ChatIntentRequest(message, mainFeatures))
                .retrieve()
                .bodyToMono(ChatIntentResponse.class);
    }

}

