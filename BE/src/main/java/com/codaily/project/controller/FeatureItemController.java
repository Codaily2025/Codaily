package com.codaily.project.controller;

import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.service.FeatureItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feature")
@RequiredArgsConstructor
public class FeatureItemController {

    private final FeatureItemService featureItemService;

    @PutMapping("/update")
    public ResponseEntity<String> updateFeatureItem(@RequestBody FeatureSaveItem request) {
        featureItemService.updateFeatureItem(request);
        return ResponseEntity.ok("기능 정보가 수정되었습니다.");
    }
}

