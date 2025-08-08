package com.codaily.codereview.controller;

import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.codereview.service.FeatureItemChecklistService;
import com.codaily.mypage.dto.ProjectStatusResponse;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.service.FeatureItemService;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feature-checklist")
@RequiredArgsConstructor
public class FeatureItemChecklistController {

    private final ProjectService projectService;
    private final FeatureItemService featureItemService;
    private final FeatureItemChecklistService featureItemChecklistService;
    private final FeatureItemChecklistRepository featureItemChecklistRepository;

    // 체크리스트 생성
    @GetMapping("/{projectId}/generate")
    public ResponseEntity<?> generateChecklist(@PathVariable Long projectId) {
        Project project = projectService.findById(projectId);
        if(project.getFeatureItems() != null && project.getSpecification() != null) {
            featureItemService.generateFeatureItemChecklist(projectId);
        }
        return ResponseEntity.ok("체크리스트 생성 완료");
    }

    // 체크리스트 삭제
    @DeleteMapping("/{featureId}/{featureItemChecklistId}")
    public ResponseEntity<?> deleteChecklist(@PathVariable Long featureId, @PathVariable Long featureItemChecklistId) {
        // 사용자가 기능을 삭제했을 때, 기능에 해당하는 체크리스트 삭제
        List<FeatureItemChecklist> checklists = featureItemChecklistService.findByFeatureItem_FeatureId(featureId);

        for(FeatureItemChecklist item : checklists) {
            featureItemChecklistRepository.delete(item);
        }

        return ResponseEntity.ok("삭제 완료");
    }

    // 체크리스트 등록
    @PostMapping("/generate/extra/{featureId}")
    public ResponseEntity<?> generateExtraChecklist(@PathVariable Long featureId) {
        // 1. 체크리스트 생성
        featureItemService.generateExtraFeatureItemChecklist(featureId);

        // 2. 생성된 체크리스트 조회
        List<FeatureItemChecklist> featureItemChecklist = featureItemChecklistService.findByFeatureItem_FeatureId(featureId);

        // 3. item 값이 null인 항목이 있는지 검사
        for (FeatureItemChecklist checklist : featureItemChecklist) {
            if (checklist.getItem() == null || checklist.getItem().trim().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body("체크리스트 항목 중 item 값이 비어 있습니다.");
            }
        }

        // 4. 성공적으로 반환
        return ResponseEntity.ok(featureItemChecklist);
    }


}
