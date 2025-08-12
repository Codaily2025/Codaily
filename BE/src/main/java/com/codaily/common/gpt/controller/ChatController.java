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
                    - `chat` : 일반 GPT 메시지 응답(단일 조각)
                    - `spec` : 명세서 전체 생성(여러 조각)
                    - `spec:regenerate` : 기존 명세 삭제 후 재생성(여러 조각)
                    - `spec:summarization` : 명세서 요약 1개 조각
                    - `spec:add:field` : 기능 그룹(필드) 단위로 주/부 기능 묶음(여러 조각)
                    - `spec:add:feature:main` : 주 기능 + 상세 기능 생성(1개 조각)
                    - `spec:add:feature:sub` : 기존 주 기능에 상세 기능 1개 추가(1개 조각)
                    
                    모든 응답은 `event: message` 형식으로 전송되며, 각 조각은 최상위 중괄호 기준으로 구분됩니다.
                    
                    ※ `isReduced` 필드:
                    - 각 기능(메인/서브) 및 조각(content) 최상위에 포함될 수 있으며, 축소(후보 제거) 상태를 나타냅니다.
                    - 최종 확정 시점에 `isReduced=true` 항목은 일괄 삭제(또는 비활성화) 처리에 사용됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "SSE 연결 성공 및 실시간 명세 응답",
                            content = @Content(
                                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                                    examples = {
                                            // chat
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
                                            // spec:summarization
                                            @ExampleObject(
                                                    name = "spec:summarization",
                                                    summary = "명세서 생성 요약 정보",
                                                    value = """
                                                            event: message
                                                            data: {
                                                              "type": "spec:summarization",
                                                              "content": {
                                                                "projectTitle": "온라인 쇼핑몰 운영 시스템",
                                                                "projectDescription": "온라인에서 상품 판매를 위한 쇼핑몰 플랫폼으로 사용자 친화적인 인터페이스와 다양한 기능들을 갖추고 있으며, 결제, 리뷰, 배송 등 고객의 편의를 위한 핵심 기능을 포함하고 있습니다.",
                                                                "specTitle": "쇼핑몰 핵심 기능 명세서",
                                                                "projectId": 3,
                                                                "specId": 1
                                                              }
                                                            }
                                                            """
                                            ),
                                            // spec / spec:regenerate / spec:add:field
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
                                                                "field": "배송 관리",
                                                                "mainFeature": {
                                                                  "id": 2822,
                                                                  "title": "배송 상태 변경",
                                                                  "description": "관리자 또는 담당자가 주문의 배송 상태를 업데이트할 수 있음",
                                                                  "estimatedTime": 3,
                                                                  "priorityLevel": null,
                                                                  "isReduced": false
                                                                },
                                                                "subFeature": [
                                                                  {
                                                                    "id": 2823,
                                                                    "title": "배송 조회",
                                                                    "description": "사용자가 자신의 주문 배송 상태를 조회할 수 있도록 정보를 제공",
                                                                    "estimatedTime": 2,
                                                                    "priorityLevel": 5,
                                                                    "isReduced": false
                                                                  },
                                                                  {
                                                                    "id": 2824,
                                                                    "title": "배송 상태 변경",
                                                                    "description": "관리자가 주문의 배송 상태를 수정할 수 있도록 기능 구현",
                                                                    "estimatedTime": 1,
                                                                    "priorityLevel": 4,
                                                                    "isReduced": false
                                                                  }
                                                                ],
                                                                "isReduced": false
                                                              }
                                                            }
                                                            """
                                            ),
                                            // spec:add:feature:main
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
                                                                  "priorityLevel": null,
                                                                  "isReduced": false
                                                                },
                                                                "subFeature": [
                                                                  {
                                                                    "id": 2086,
                                                                    "title": "리뷰 작성 화면 표시",
                                                                    "description": "사용자가 상품 리뷰를 작성할 수 있는 페이지를 화면에 보여줍니다",
                                                                    "estimatedTime": 2,
                                                                    "priorityLevel": 4,
                                                                    "isReduced": false
                                                                  },
                                                                  {
                                                                    "id": 2087,
                                                                    "title": "리뷰 내용 입력 처리",
                                                                    "description": "사용자가 리뷰 내용을 입력할 수 있게 텍스트박스를 활성화합니다",
                                                                    "estimatedTime": 1,
                                                                    "priorityLevel": 7,
                                                                    "isReduced": false
                                                                  }
                                                                ],
                                                                "isReduced": false
                                                              }
                                                            }
                                                            """
                                            ),
                                            // spec:add:feature:sub
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
                                                                  "priorityLevel": 7,
                                                                  "isReduced": false
                                                                },
                                                                "isReduced": false
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
