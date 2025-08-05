package com.codaily.project.controller;

import com.codaily.project.dto.FeatureItemCreate;
import com.codaily.project.dto.FeatureItemResponse;
import com.codaily.project.dto.FeatureItemUpdate;
import com.codaily.project.service.FeatureItemServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/features")
@RequiredArgsConstructor
public class FeatureItemController {

    private final FeatureItemServiceImpl featureItemServiceImpl;

    @PostMapping
    public ResponseEntity<FeatureItemResponse> createFeature(
            @PathVariable Long projectId,
            @RequestBody FeatureItemCreate create) {
        FeatureItemResponse createdFeature = featureItemServiceImpl.createFeature(projectId, create);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFeature);
    }

    @GetMapping("/{featureId}")
    public ResponseEntity<FeatureItemResponse> getFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId){
        FeatureItemResponse feature = featureItemServiceImpl.getFeature(projectId, featureId);
        return ResponseEntity.ok(feature);
    }
    private final FeatureItemService featureItemService;

    @PutMapping("/{featureId}")
    public ResponseEntity<FeatureItemResponse> updateFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId,
            @RequestBody FeatureItemUpdate update) {
        FeatureItemResponse updatedFeature = featureItemServiceImpl.updateFeature(projectId, featureId, update);
        return ResponseEntity.ok(updatedFeature);
    }
    @PutMapping("/update")
    public ResponseEntity<String> updateFeatureItem(@RequestBody FeatureSaveItem request) {
        featureItemService.updateFeatureItem(request);
        return ResponseEntity.ok("기능 정보가 수정되었습니다.");
    }

    @DeleteMapping("/{featureId}")
    public ResponseEntity<Void> deleteFeature(
            @PathVariable Long projectId,
            @PathVariable Long featureId) {
        featureItemServiceImpl.deleteFeature(projectId, featureId);
        return ResponseEntity.noContent().build();
    }

}
