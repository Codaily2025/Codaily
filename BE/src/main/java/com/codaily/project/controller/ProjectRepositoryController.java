package com.codaily.project.controller;

import com.codaily.project.dto.ProjectRepositoriesResponse;
import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Project Repository API", description = "GitHub 리포지토리 조회 및 연결 해제 API")
public class ProjectRepositoryController {

    private final ProjectService projectService;

    @GetMapping("/projects/{projectId}/repos")
    @Operation(summary = "프로젝트 리포지토리 조회", description = "특정 프로젝트에 연결된 GitHub 리포지토리 목록을 조회합니다.")
    public ResponseEntity<ProjectRepositoriesResponse> getProjectRepositories(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId
    ) {
        List<ProjectRepositoryResponse> repositories =
                projectService.getRepositoriesByProjectId(projectId);
        return ResponseEntity.ok(new ProjectRepositoriesResponse(repositories));
    }

    @DeleteMapping("/repos/{repoId}")
    @Operation(summary = "리포지토리 연결 해제", description = "프로젝트에서 특정 GitHub 리포지토리 연결을 해제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "리포지토리 연결 해제 완료"),
//            @ApiResponse(responseCode = "404", description = "존재하지 않는 리포지토리 ID"),
//            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Void> unlinkRepository(
            @Parameter(description = "리포지토리 ID") @PathVariable Long repoId
    ) {
        projectService.deleteRepositoryById(repoId);
        return ResponseEntity.noContent().build();
    }

}
