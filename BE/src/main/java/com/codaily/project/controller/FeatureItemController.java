package com.codaily.project.controller;

import com.codaily.project.dto.*;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.service.FeatureFieldService;
import com.codaily.project.service.FeatureItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/features")
@RequiredArgsConstructor
@Tag(name = "Feature Item API", description = "프로젝트의 기능 명세 및 일정 관련 API")
public class FeatureItemController {

    private final FeatureItemService featureItemService;
    private final FeatureFieldService featureFieldService;

    @PostMapping("/schedule")
    @Operation(summary = "초기 일정 생성", description = "요구사항 명세서 생성 후 첫 일정을 자동으로 계산하여 생성합니다.")
    public ResponseEntity<String> scheduleProject(@Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        featureItemService.scheduleProjectInitially(projectId);
        return ResponseEntity.ok("프로젝트 스케줄링이 완료되었습니다.");
    }

    @PostMapping
    @Operation(summary = "수동 기능 추가", description = "보드에 직접 기능을 수동으로 추가합니다.")
    public ResponseEntity<FeatureItemResponse> createFeature(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @RequestBody FeatureItemCreateRequest create) {
        FeatureItemResponse createdFeature = featureItemService.createFeature(projectId, create);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFeature);
    }

    @GetMapping("/{featureId}")
    @Operation(summary = "기능 상세 조회", description = "기능 ID를 통해 해당 기능의 상세 정보를 반환합니다.")
    public ResponseEntity<FeatureItemResponse> getFeature(
            @PathVariable Long projectId,
            @Parameter(description = "기능 ID") @PathVariable Long featureId) {
        FeatureItemResponse feature = featureItemService.getFeature(projectId, featureId);
        return ResponseEntity.ok(feature);
    }

    @PutMapping("/{featureId}")
    @Operation(summary = "기능 수정", description = "우선순위, 예상 시간 변경 등의 수정을 수행합니다.")
    public ResponseEntity<FeatureItemResponse> updateFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @RequestBody FeatureItemUpdateRequest update) {
        FeatureItemResponse updatedFeature = featureItemService.updateFeature(projectId, featureId, update);
        return ResponseEntity.ok(updatedFeature);
    }

//    @PutMapping("/update")
//    @Operation(summary = "기능 정보 일괄 수정", description = "명세서에 따라 기능 정보를 업데이트합니다.")
//    public ResponseEntity<String> updateFeatureItem(@RequestBody FeatureSaveItem request) {
//        featureItemService.updateFeatureItem(request);
//        return ResponseEntity.ok("기능 정보가 수정되었습니다.");
//    }

    @DeleteMapping("/{featureId}")
    @Operation(summary = "기능 삭제", description = "특정 기능을 삭제합니다.")
    public ResponseEntity<Void> deleteFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId) {
        featureItemService.deleteFeature(projectId, featureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/field-tabs")
    @Operation(summary = "대주제 탭 목록 조회", description = "프로젝트의 기능 대주제(필드) 탭 목록을 반환합니다.")
    public ResponseEntity<List<String>> getFieldTabs(@PathVariable Long projectId) {
        List<String> fieldTabs = featureFieldService.getFieldTabs(projectId);
        return ResponseEntity.ok(fieldTabs);
    }

    @GetMapping("/field/{field}/by-status")
    @Operation(summary = "대주제 및 상태별 기능 조회", description = "대주제(필드) 및 상태(TODO, IN_PROGRESS 등)별로 기능을 분류하여 조회합니다.")
    public ResponseEntity<Map<String, List<FeatureItem>>> getFeaturesByFieldAndStatus(
            @PathVariable Long projectId,
            @Parameter(description = "대주제 이름") @PathVariable String field) {
        Map<String, List<FeatureItem>> result = featureFieldService.getFeaturesByFieldAndStatus(projectId, field);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{featureId}/status")
    @Operation(summary = "기능 상태 변경", description = "칸반 보드의 기능 상태를 변경합니다. (예: TODO → DONE)")
    public ResponseEntity<FeatureItem> updateFeatureStatus(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @RequestBody UpdateStatusRequest request
    ) {
        FeatureItem updateFeature = featureFieldService.updateFeatureStatus(featureId, request.getNewStatus());
        return ResponseEntity.ok(updateFeature);
    }
}
