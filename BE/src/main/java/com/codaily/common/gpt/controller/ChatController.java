package com.codaily.common.gpt.controller;

import com.codaily.common.gpt.handler.ChatResponseStreamHandler;
import com.codaily.common.gpt.handler.MessageType;
import com.codaily.common.gpt.service.ChatService;
import com.codaily.project.service.FeatureItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Log4j2
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat API", description = "GPT와의 실시간 채팅 및 명세서 생성 기능")
public class ChatController {

    private final ChatService chatService;
    private final FeatureItemService featureItemService;
    private final ChatResponseStreamHandler chatStreamHandler;

    @Operation(
            summary = "실시간 GPT 채팅 (SSE)",
            description = """
        사용자 메시지를 분석하여 intent를 분류한 뒤,
        GPT 응답을 실시간으로 SSE로 반환합니다.

        ### 응답 형식
        - intent = `chat` → ChatMessageResponse
        - intent = `spec`, `spec:regenerate` → FeatureSaveResponse
        """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 연결 성공",
                            content = @Content(
                                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "chat",
                                                    summary = "일반 GPT 채팅 응답",
                                                    value = """
                        event: message
                        data: {
                          "type": "chat",
                          "content": "안녕하세요! 무엇을 도와드릴까요?"
                        }
                        """
                                            ),
                                            @ExampleObject(
                                                    name = "spec",
                                                    summary = "명세서 기능 응답",
                                                    value = """
                        event: message
                        data: {
                          "type": "spec",
                          "content": {
                            "projectId": 1,
                            "specId": 10,
                            "mainFeature": {
                              "id": 1,
                              "title": "로그인 기능",
                              "description": "사용자 인증을 처리합니다.",
                              "estimatedTime": 1.5,
                              "priorityLevel": 1
                            },
                            "subFeature": [
                              {
                                "id": 2,
                                "title": "이메일 입력",
                                "description": "사용자 이메일을 입력받습니다.",
                                "estimatedTime": 0.5,
                                "priorityLevel": 2
                              }
                            ]
                          }
                        }
                        """
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("userId") String userId,
                                 @RequestParam("message") String message,
                                 @RequestParam("projectId") Long projectId,
                                 @RequestParam("specId") Long specId) {

        String intent = chatService.classifyIntent(message).block();
        MessageType messageType = MessageType.fromString(intent);

        switch (messageType) {
            case SPEC:
            case SPEC_REGENERATE:
                featureItemService.deleteBySpecId(specId);
                break;
            case CHAT:
                break;
        }

        return chatStreamHandler.stream(intent, userId, message, projectId, specId);
    }
}
