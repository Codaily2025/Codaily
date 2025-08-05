package com.codaily.common.gpt.controller;

import com.codaily.common.gpt.handler.MessageType;
import com.codaily.common.gpt.handler.ChatResponseStreamHandler;
import com.codaily.common.gpt.service.ChatService;
import com.codaily.project.service.FeatureItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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
            summary = "실시간 GPT 채팅",
            description = "사용자의 메시지를 분석하여 GPT 응답을 실시간으로 스트리밍합니다. 명세서 생성을 위한 intent 판단도 포함됩니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자 ID", required = true),
                    @Parameter(name = "message", description = "사용자 입력 메시지", required = true),
                    @Parameter(name = "projectId", description = "프로젝트 ID", required = true),
                    @Parameter(name = "specId", description = "명세서 ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "SSE 연결 성공", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)),
//                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
//                    @ApiResponse(responseCode = "500", description = "서버 오류")
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
