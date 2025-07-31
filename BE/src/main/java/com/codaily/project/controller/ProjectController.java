package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/project")
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
}
