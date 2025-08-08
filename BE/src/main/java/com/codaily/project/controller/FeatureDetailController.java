package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.*;
import com.codaily.project.service.FeatureDetailService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class FeatureDetailController {

    private final FeatureDetailService featureDetailService;

    //기능 상세 정보 조회
    @Operation(summary = "기능 상세 조회", description = "기능의 상세 정보와 하위 기능들을 조회합니다")
    @GetMapping("/{projectId}/features/{featureId}/details")
    public ResponseEntity<FeatureDetailResponse> getFeatureDetail(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("기능 상세 조회 요청 - projectId: {}, featureId: {}, userId: {}",
                projectId, featureId, userDetails.getUserId());

        FeatureDetailResponse response = featureDetailService.getFeatureDetail(
                projectId, featureId, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    //기능 정보 수정
    @Operation(summary = "기능 정보 수정")
    @PutMapping("/{projectId}/features/{featureId}/details")
    public ResponseEntity<FeatureDetailResponse> updateFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @RequestBody FeatureUpdateRequest request,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("기능 수정 요청 - projectId: {}, featureId: {}, userId: {}",
                projectId, featureId, userDetails.getUserId());

        FeatureDetailResponse response = featureDetailService.updateFeature(
                projectId, featureId, request, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

//    //기능 상태만 수정 (간단한 상태 변경용)
//    @Operation(summary = "기능 상태 수정")
//    @PatchMapping("/{projectId}/features/{featureId}/status")
//    public ResponseEntity<FeatureDetailResponse> updateFeatureStatus(
//            @PathVariable Long projectId,
//            @PathVariable Long featureId,
//            @RequestParam String status,
//            @AuthenticationPrincipal PrincipalDetails userDetails) {
//
//        log.info("기능 상태 변경 요청 - projectId: {}, featureId: {}, status: {}, userId: {}",
//                projectId, featureId, status, userDetails.getUserId());
//
//        FeatureUpdateRequest request = FeatureUpdateRequest.builder()
//                .status(status)
//                .build();
//
//        FeatureDetailResponse response = featureDetailService.updateFeature(
//                projectId, featureId, request, userDetails.getUserId());
//
//        return response.isSuccess() ?
//                ResponseEntity.ok(response) :
//                ResponseEntity.badRequest().body(response);
//    }

    //하위 기능 생성
    @Operation(summary = "하위 기능 생성")
    @PostMapping("/{projectId}/features/{featureId}/subfeatures")
    public ResponseEntity<SubFeatureCreateResponse> createSubFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @RequestBody SubFeatureCreateRequest request,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("하위 기능 생성 요청 - projectId: {}, parentFeatureId: {}, userId: {}",
                projectId, featureId, userDetails.getUserId());

        SubFeatureCreateResponse response = featureDetailService.createSubFeature(
                projectId, featureId, request, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    //캘린더 특정 날짜의 기능 정보 조회
    @Operation(summary = "캘린더 특정 날짜의 기능 정보 조회")
    @GetMapping("/{projectId}/calendar/{date}")
    public ResponseEntity<CalendarFeatureResponse> getCalendarFeatures(
            @PathVariable Long projectId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("캘린더 기능 조회 요청 - projectId: {}, date: {}, userId: {}",
                projectId, date, userDetails.getUserId());

        CalendarFeatureResponse response = featureDetailService.getCalendarFeatures(
                projectId, date, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    //기능의 하위 기능 목록 조회
    @Operation(summary = "기능의 하위 기능 목록 조회")
    @GetMapping("/{projectId}/features/{featureId}/subfeatures")
    public ResponseEntity<SubFeatureListResponse> getSubFeatures(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("하위 기능 목록 조회 요청 - projectId: {}, featureId: {}, userId: {}",
                projectId, featureId, userDetails.getUserId());

        SubFeatureListResponse response = featureDetailService.getSubFeatures(
                projectId, featureId, userDetails.getUserId());

        return response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response);
    }

    //기능 삭제 (하위 기능도 함께 삭제)
    @Operation(summary = "기능 삭제", description = "기능과 모든 하위 기능들을 삭제합니다")
    @DeleteMapping("/{projectId}/features/{featureId}")
    public ResponseEntity<Void> deleteFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("기능 삭제 요청 - projectId: {}, featureId: {}, userId: {}",
                projectId, featureId, userDetails.getUserId());

        featureDetailService.deleteFeature(projectId, featureId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }


}