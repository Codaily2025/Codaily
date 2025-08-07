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
                        사용자 메시지를 분석하여 intent를 분류한 뒤,
                        해당 intent에 맞는 응답을 실시간 SSE로 반환합니다.
                    
                        ### intent별 응답 및 조각 구성
                        #### 1. 명세서 생성 요청 (`spec`)
                        - 전체 명세서 요약 정보가 포함된 JSON 한 조각 전송
                        - 예: `"온라인 쇼핑몰 플랫폼 개발"` 명세서 요약
                    
                        #### 2. 필드별 기능명세 (`spec:add:field`)
                        - 한 필드에 대한 주기능 + 상세기능 묶음이 여러 조각으로 순차 전송
                        - 각 조각은 다음 구조를 가짐:
                            ```json
                            {
                              "projectId": 3,
                              "specId": 1,
                              "field": "배송",
                              "mainFeature": { ... },
                              "subFeature": [ ... ]
                            }
                            ```
                    
                        #### 3. 주 기능 추가 (`spec:add:feature:main`)
                        - 사용자 입력으로 특정 주 기능과 그에 따른 상세 기능을 생성
                        - 조각 형태는 위 `spec:add:field`와 동일
                        - 예: `"상품 리뷰를 남길 수 있었으면 좋겠어요"` → 리뷰 작성 기능 추가
                    
                        #### 4. 상세 기능 추가 (`spec:add:feature:sub`)
                        - 기존 주 기능에 상세 기능 하나를 추가하는 단일 조각 전송
                        - 예: `"결제할 때 포인트를 사용할 수 있었으면 좋겠어요"` → 아래 구조로 전송:
                            ```json
                            {
                              "projectId": 3,
                              "specId": 1,
                              "parentFeatureId": 2078,
                              "featureSaveItem": {
                                "id": 2084,
                                "title": "포인트 사용 선택 인터페이스 표시",
                                ...
                              }
                            }
                            ```
                    
                        #### 5. 일반 채팅 (`chat`)
                        - GPT의 자연어 응답이 단일 메시지로 전송됨
                            ```json
                            {
                              "type": "chat",
                              "content": "안녕하세요! 무엇을 도와드릴까요?"
                            }
                            ```
                    
                        ---
                        모든 응답은 `event: message`로 전송되며, 각 조각은 가장 바깥 중괄호 단위로 분리됩니다.
                        복수 조각이 순차 전송될 수 있습니다 (특히 `spec:add:field`, `spec`).
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
                                                              "content": "안녕하세요! 무엇을 도와드릴까요?"
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
