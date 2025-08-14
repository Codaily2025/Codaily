package com.codaily.codereview.controller;

import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.codereview.service.FeatureItemChecklistService;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.service.FeatureItemService;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
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
    private final FeatureItemRepository featureItemRepository;
    private final FeatureItemChecklistService featureItemChecklistService;
    private final FeatureItemChecklistRepository featureItemChecklistRepository;

    // 체크리스트 생성
    @GetMapping("/{projectId}/generate")
    @Operation(summary = "체크리스트 생성", description = "projectId 입력 / 사용자가 프로젝트 생성하기 버튼을 클릭했을 때 실행되어야 합니다")
    public ResponseEntity<?> generateChecklist(@PathVariable Long projectId) {
        Project project = projectService.findById(projectId);

        if(project.getFeatureItems() != null && project.getSpecification() != null) {
            featureItemService.generateFeatureItemChecklist(projectId);
        }
        return ResponseEntity.ok("체크리스트 생성 완료");
    }

    // 체크리스트 삭제
    @DeleteMapping("/{featureId}")
    @Operation(summary = "체크리스트 삭제", description = "featureId 입력 / 사용자가 기능을 삭제했을 때 같이 실행되어야 합니다")
    public ResponseEntity<?> deleteChecklist(@PathVariable Long featureId) {
        // item 의 feature_id 가 가지고 있는 parent_feature_id가 1개 라면? parent_feature_Id 삭제
        FeatureItem featureItem = featureItemRepository.getFeatureItemByFeatureId(featureId);

        // 사용자가 기능을 삭제했을 때, 기능에 해당하는 체크리스트 삭제
        List<FeatureItemChecklist> checklists = featureItemChecklistService.findByFeatureItem_FeatureId(featureId);

        for(FeatureItemChecklist item : checklists) {
            featureItemChecklistRepository.delete(item);
        }

        return ResponseEntity.ok("삭제 완료");
    }

    // 체크리스트 등록
    @PostMapping("/generate/extra/{featureId}")
    @Operation(summary = "체크리스트 추가 생성", description = "featureId 입력 / 프로젝트 진행 중에 사용자가 기능을 추가했을 때 실행되어야 합니다")
    public ResponseEntity<?> generateExtraChecklist(@PathVariable Long featureId) {
        // 1. 체크리스트 생성
        boolean valid = featureItemService.generateExtraFeatureItemChecklist(featureId);

        // 프론트와 논의 필요
        if(!valid) {
            return ResponseEntity.ok("null");
        }

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
