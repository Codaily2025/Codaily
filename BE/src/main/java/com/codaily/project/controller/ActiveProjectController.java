package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.ActiveProjectsResponse;
import com.codaily.project.service.ActiveProjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ActiveProjectController {

    private final ActiveProjectService activeProjectService;

    @Operation(summary = "활성 프로젝트 조회")
    @GetMapping("/projects/active")
    public ResponseEntity<ActiveProjectsResponse> getActiveProjects(
            @AuthenticationPrincipal PrincipalDetails userDetails) {

        log.info("활성 프로젝트 조회 요청 - userId: {}", userDetails.getUserId());

        try {
            ActiveProjectsResponse response = activeProjectService.getActiveProjects(userDetails.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("활성 프로젝트 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(ActiveProjectsResponse.builder()
                            .success(false)
                            .build());
        }
    }
}