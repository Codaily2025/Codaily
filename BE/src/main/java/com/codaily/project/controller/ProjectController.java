package com.codaily.project.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.project.dto.FeatureItemReduceResponse;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project API", description = "프로젝트 생성 및 기능 명세 축소 관련 API")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "프로젝트 생성", description = "신규 프로젝트를 생성하고 기본 명세도 함께 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프로젝트 및 명세 생성 성공", headers = {
                    @Header(name = "Location", description = "생성된 프로젝트 URI")
            }),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Map<String, Long>> createProject(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestBody ProjectCreateRequest request
    ) {
        Project project = projectService.createProject(request, userDetails.getUser());
        Specification spec = project.getSpecification(); // 또는 명세 생성 메서드 결과

        Map<String, Long> responseBody = Map.of(
                "projectId", project.getProjectId(),
                "specId", spec.getSpecId()
        );

        URI location = URI.create("/api/projects/" + project.getProjectId());
        return ResponseEntity.created(location).body(responseBody);
    }


    @PostMapping("/{projectId}/specs/{specId}/features/reduce")
    @Operation(summary = "기능 축소", description = "기능이 너무 많은 경우 일부 기능을 자동 축소합니다.")
    public ResponseEntity<FeatureItemReduceResponse> reduceFeatures(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(description = "명세서 ID") @PathVariable Long specId
    ) {
        FeatureItemReduceResponse response = projectService.reduceFeatureItemsIfNeeded(projectId, specId);
        return ResponseEntity.ok(response);
    }

}
