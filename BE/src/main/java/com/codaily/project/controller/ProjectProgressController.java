package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.ProjectProgressResponse;
import com.codaily.project.service.ProjectProgressService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectProgressController {

    private final ProjectProgressService projectProgressService;

    //프로젝트 진행률 조회
    @Operation(summary = "프로젝트 진행률 조회")
    @GetMapping("/projects/{projectId}/progress")
    public ResponseEntity<ProjectProgressResponse> getProjectProgress(
            @PathVariable Long projectId,
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("프로젝트 진행률 조회 요청 - projectId: {}, userId: {}",
                projectId, userDetails.getUserId());

        try {
            ProjectProgressResponse response = projectProgressService.getProjectProgress(projectId);

            if (response.isSuccess()) {
                log.info("프로젝트 진행률 조회 성공 - projectId: {}, percentage: {}%",
                        projectId, response.getData().getOverallProgress().getPercentage());
                return ResponseEntity.ok(response);
            } else {
                log.warn("프로젝트 진행률 조회 실패 - projectId: {}", projectId);
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            log.error("프로젝트 진행률 조회 중 예외 발생 - projectId: {}, error: {}",
                    projectId, e.getMessage());

            ProjectProgressResponse errorResponse = ProjectProgressResponse.builder()
                    .success(false)
                    .build();

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}