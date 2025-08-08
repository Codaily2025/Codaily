package com.codaily.project.controller;

import com.codaily.project.dto.*;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.service.FeatureFieldService;
import com.codaily.project.service.FeatureItemService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/features")
@RequiredArgsConstructor
public class FeatureItemController {

    private final FeatureItemService featureItemService;
    private final FeatureFieldService featureFieldService;

    @PostMapping("/schedule")
    @Operation(summary = "초기 일정 생성", description = "요구사항 명세서 생성 후 첫 일정 생성")
    public ResponseEntity<String> scheduleProject(@PathVariable Long projectId) {
        featureItemService.scheduleProjectInitially(projectId);
        return ResponseEntity.ok("프로젝트 스케줄링이 완료되었습니다.");
    }

    @PostMapping
    @Operation(summary = "수동 기능 추가", description = "프로젝트 보드에서 기능 추가")
    public ResponseEntity<FeatureItemResponse> createFeature(
            @PathVariable Long projectId,
            @RequestBody FeatureItemCreateRequest create) {
        FeatureItemResponse createdFeature = featureItemService.createFeature(projectId, create);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFeature);
    }

    @GetMapping("/{featureId}")
    @Operation(summary = "기능 상세 조회")
    public ResponseEntity<FeatureItemResponse> getFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId) {
        FeatureItemResponse feature = featureItemService.getFeature(projectId, featureId);
        return ResponseEntity.ok(feature);
    }

    @PutMapping("/{featureId}")
    @Operation(summary = "기능 수정", description = "우선순위, 예상시간 수정 시 일정 재생성")
    public ResponseEntity<FeatureItemResponse> updateFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @RequestBody FeatureItemUpdateRequest update) {
        FeatureItemResponse updatedFeature = featureItemService.updateFeature(projectId, featureId, update);
        return ResponseEntity.ok(updatedFeature);
    }

//    @PutMapping("/update")
//    public ResponseEntity<String> updateFeatureItem(@RequestBody FeatureSaveItem request) {
//        featureItemService.updateFeatureItem(request);
//        return ResponseEntity.ok("기능 정보가 수정되었습니다.");
//    }
//
//    @DeleteMapping("/{featureId}")
//    @Operation(summary = "기능 삭제")
//    public ResponseEntity<Void> deleteFeature(
//            @PathVariable Long projectId,
//            @PathVariable Long featureId) {
//        featureItemService.deleteFeature(projectId, featureId);
//        return ResponseEntity.noContent().build();
//    }

    @GetMapping("/field-tabs")
    @Operation(summary = "대주제별 조회", description = "칸반 대주제 탭")
    public ResponseEntity<List<String>> getFieldTabs(@PathVariable Long projectId) {
        List<String> fieldTabs = featureFieldService.getFieldTabs(projectId);
        return ResponseEntity.ok(fieldTabs);
    }

    @GetMapping("/field/{field}/by-status")
    @Operation(summary = "대주제 & 상태별 조회", description = "대주제 탭 하단 칸반 상태 조회")
    public ResponseEntity<Map<String, List<FeatureItem>>> getFeaturesByFieldAndStatus(@PathVariable Long projectId, @PathVariable String field) {
        Map<String, List<FeatureItem>> result = featureFieldService.getFeaturesByFieldAndStatus(projectId, field);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{featureId}/status")
    @Operation(summary = "칸반 status 변경", description = "TODO, IN_PROGRESS, DONE")
    public ResponseEntity<FeatureItem> updateFeatureStatus(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @RequestBody UpdateStatusRequest request
    ) {
        FeatureItem updateFeature = featureFieldService.updateFeatureStatus(featureId, request.getNewStatus());
        return ResponseEntity.ok(updateFeature);
    }
}
