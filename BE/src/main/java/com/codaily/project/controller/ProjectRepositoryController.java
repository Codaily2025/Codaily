package com.codaily.project.controller;

import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectRepositoryController {
    public final ProjectService projectService;

    @GetMapping("/repos")
    public ResponseEntity<List<ProjectRepositoryResponse>> getProjectRepositories(
            @RequestParam Long projectId
    ) {
        List<ProjectRepositoryResponse> repositories =
                projectService.getRepositoriesByProjectId(projectId);
        return ResponseEntity.ok(repositories);
    }

    @DeleteMapping("/repo")
    public ResponseEntity<String> unlinkRepository(
            @RequestParam Long repoId
    ) {
        projectService.deleteRepositoryById(repoId);
        return ResponseEntity.ok("리포지토리 연결 해제 완료");
    }
}
