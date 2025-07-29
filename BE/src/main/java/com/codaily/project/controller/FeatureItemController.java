package com.codaily.project.controller;

import com.codaily.project.dto.FeatureItemCreate;
import com.codaily.project.dto.FeatureItemResponse;
import com.codaily.project.service.FeatureItemServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
