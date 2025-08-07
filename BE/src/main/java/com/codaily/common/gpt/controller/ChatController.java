package com.codaily.common.gpt.controller;

import com.codaily.common.gpt.dto.ChatIntentResponse;
import com.codaily.common.gpt.dto.ChatStreamRequest;
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
                    사용자 메시지를 분석해 intent를 분류한 뒤, 해당 intent에 맞는 응답을 실시간 SSE로 전송합니다.
                    
                    intent는 다음과 같이 분류됩니다:
                    
                    - `chat` : 일반 GPT 메시지 응답입니다. 단일 텍스트 조각이 전송됩니다.
                    
                    - `spec` : 명세서 전체 생성 요청이며, 주/부 기능 조각 여러 개가 순차적으로 전송됩니다. (`spec:add:field`와 구조 유사)
                    
                    - `spec:regenerate` : 기존 명세서를 삭제하고 새로 생성하며, `spec`과 동일하게 복수 조각이 전송됩니다.
                    
                    - `spec:summarization` : 명세서 요약 정보를 1개 조각으로 전송합니다.
                    
                    - `spec:add:field` : 기능 그룹(필드) 단위로 주기능 + 상세기능을 하나의 조각으로 묶어 여러 개 순차 전송합니다.
                    
                    - `spec:add:feature:main` : 사용자의 요청에 따라 주기능과 상세기능을 생성하여 1개 조각으로 전송합니다.
                    
                    - `spec:add:feature:sub` : 기존 주기능에 상세기능 하나를 추가하며, 해당 기능만을 담은 단일 조각이 전송됩니다.
                    
                    모든 응답은 `event: message` 형식으로 전송되며, 각 조각은 최상위 중괄호 기준으로 구분됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 연결 성공 및 실시간 명세 응답",
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
                                                              "content": "안"
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "spec:summarization",
                                                    summary = "명세서 생성 요약 정보",
                                                    value = """
                                                            event: message
                                                            data: {
                                                              "type": "project:summarization",
                                                              "content": {
                                                                "projectTitle": "온라인 쇼핑몰 플랫폼 개발",
                                                                "projectDescription": "사용자들이 온라인으로 상품을 구매할 수 있는 쇼핑몰 웹사이트를 개발하는 프로젝트입니다.",
                                                                "specTitle": "온라인 쇼핑몰 플랫폼 명세서",
                                                                "projectId": 1,
                                                                "specId": 1
                                                              }
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "spec, spec:regenerate, spec:add:field",
                                                    summary = "기능 그룹(필드) 단위 기능 응답",
                                                    value = """
                                                            event: message
                                                            data: {
                                                              "type": "spec OR spec:regenerate OR spec:add:field",
                                                              "content": {
                                                                "projectId": 3,
                                                                "specId": 1,
                                                                "field": "배송",
                                                                "mainFeature": {
                                                                  "id": 2088,
                                                                  "title": "배송 조회",
                                                                  "description": "사용자와 관리자 모두 배송 상태와 위치를 확인할 수 있음",
                                                                  "estimatedTime": 3,
                                                                  "priorityLevel": null
                                                                },
                                                                "subFeature": [
                                                                  {
                                                                    "id": 2089,
                                                                    "title": "배송 상태 조회",
                                                                    "description": "사용자와 관리자가 배송의 현재 상태를 확인하는 기능을 수행함",
                                                                    "estimatedTime": 1,
                                                                    "priorityLevel": 8
                                                                  },
                                                                  {
                                                                    "id": 2090,
                                                                    "title": "배송 위치 추적",
                                                                    "description": "시스템이 배송 차량 또는 택배 위치 정보를 제공하여 사용자와 관리자가 현재 위치를 실시간으로 볼 수 있게 함",
                                                                    "estimatedTime": 2,
                                                                    "priorityLevel": 7
                                                                  }
                                                                ]
                                                              }
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "spec:add:feature:main",
                                                    summary = "주 기능 추가 응답",
                                                    value = """
                                                            event: message
                                                            data: {
                                                              "type": "spec:add:feature:main",
                                                              "content": {
                                                                             "projectId": 3,
                                                                             "specId": 1,
                                                                             "field": "상품 관리 및 카탈로그 기능",
                                                                             "mainFeature": {
                                                                                 "id": 2085,
                                                                                 "title": "상품 리뷰쓰기",
                                                                                 "description": "사용자가 구매한 상품에 대한 후기를 남길 수 있음",
                                                                                 "estimatedTime": 3,
                                                                                 "priorityLevel": null
                                                                             },
                                                                             "subFeature": [
                                                                                 {
                                                                                     "id": 2086,
                                                                                     "title": "리뷰 작성 화면 표시",
                                                                                     "description": "사용자가 상품 리뷰를 작성할 수 있는 페이지를 화면에 보여줍니다",
                                                                                     "estimatedTime": 2,
                                                                                     "priorityLevel": 4
                                                                                 },
                                                                                 {
                                                                                     "id": 2087,
                                                                                     "title": "리뷰 내용 입력 처리",
                                                                                     "description": "사용자가 리뷰 내용을 입력할 수 있게 텍스트박스를 활성화합니다",
                                                                                     "estimatedTime": 1,
                                                                                     "priorityLevel": 7
                                                                                 }
                                                                             ]
                                                                         }
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "spec:add:feature:sub",
                                                    summary = "상세 기능 추가 응답",
                                                    value = """
                                                            event: message
                                                            data: {
                                                              "type": "spec:add:feature:sub",
                                                              "content": {
                                                                "projectId": 3,
                                                                "specId": 1,
                                                                "parentFeatureId": 2078,
                                                                "featureSaveItem": {
                                                                  "id": 2084,
                                                                  "title": "포인트 사용 선택 인터페이스 표시",
                                                                  "description": "사용자가 결제 시 포인트를 사용할 수 있도록 선택할 수 있는 옵션을 화면에 표시",
                                                                  "estimatedTime": 2,
                                                                  "priorityLevel": 7
                                                                }
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
        ChatIntentResponse result = chatService.classifyIntent(message, projectId).block();
        MessageType messageType = MessageType.fromString(result.getIntent());
        log.info("intent result: {}", result);
        switch (messageType) {
            case SPEC:
            case SPEC_REGENERATE:
                featureItemService.deleteBySpecId(specId);
                break;
            case CHAT:
                break;
            case SPEC_ADD_FIELD:
                break;
            case SPEC_ADD_FEATURE_MAIN:
                break;
            case SPEC_ADD_FEATURE_SUB:
                break;
        }

        ChatStreamRequest streamRequest = new ChatStreamRequest(
                result.getIntent(),
                userId,
                message,
                projectId,
                specId,
                result.getFeatureId(),
                result.getField()
        );
        return chatStreamHandler.stream(streamRequest);
    }
}
