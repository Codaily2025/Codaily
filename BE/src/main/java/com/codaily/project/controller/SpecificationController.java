package com.codaily.project.controller;

import com.codaily.common.gpt.handler.ChatResponseStreamHandler;
import com.codaily.project.dto.SpecificationTimeResponse;
import com.codaily.project.service.FeatureItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/specifications")
@RequiredArgsConstructor
public class SpecificationController {

    private final ChatResponseStreamHandler chatResponseStreamHandler;
    private final FeatureItemService featureItemService;

    @PostMapping(value = "/{specId}/regenerate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter regenerate(@PathVariable Long specId,
                                 @RequestParam Long projectId,
                                 @RequestParam(defaultValue = "system") String userId) {
        String defaultMessage = "[SYSTEM] 명세서를 다시 생성해 주세요.";
        return chatResponseStreamHandler.stream("spec:regenerate", userId, defaultMessage, projectId, specId);
    }

    @GetMapping("/{specId}/total-time")
    public ResponseEntity<SpecificationTimeResponse> getTotalEstimatedTime(@PathVariable Long specId) {
        int totalTime = featureItemService.calculateTotalEstimatedTime(specId);
        SpecificationTimeResponse response = new SpecificationTimeResponse(specId, totalTime);
        return ResponseEntity.ok(response);
    }

}
