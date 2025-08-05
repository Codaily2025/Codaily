package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.FeatureItemReduceResponse;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<String> createProject(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestBody ProjectCreateRequest request
    ) {
        projectService.createProject(request, userDetails.getUser());
        return ResponseEntity.ok("프로젝트 생성 완료");
    }

    @PostMapping("/{projectId}/specs/{specId}/features/reduce")
    public ResponseEntity<FeatureItemReduceResponse> reduceFeatures(
            @PathVariable Long projectId,
            @PathVariable Long specId
    ) {
        FeatureItemReduceResponse response = projectService.reduceFeatureItemsIfNeeded(projectId, specId);
        return ResponseEntity.ok(response);
    }

}
