package com.codaily.project.controller;

import com.codaily.project.dto.ProjectRepositoriesResponse;
import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Project Repository API", description = "GitHub 리포지토리 조회 및 연결 해제 API")
public class ProjectRepositoryController {

    private final ProjectService projectService;

//    @Operation(
//            summary = "프로젝트 리포지토리 조회",
//            description = "특정 프로젝트에 연결된 GitHub 리포지토리 목록을 조회합니다.",
//            parameters = {
//                    @Parameter(name = "projectId", description = "프로젝트 ID", required = true, example = "1")
//            },
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "연결된 리포지토리 목록 반환",
//                            content = @Content(
//                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                                    examples = @ExampleObject(
//                                            name = "리포지토리 목록 응답 예시",
//                                            value = """
//                        {
//                          "repositories": [
//                            {
//                              "repoId": 1001,
//                              "repoName": "shopping-cart-service",
//                              "repoUrl": "https://github.com/example/shopping-cart-service",
//                              "createdAt": "2025-08-07T10:30:00"
//                            },
//                            {
//                              "repoId": 1002,
//                              "repoName": "order-processing-api",
//                              "repoUrl": "https://github.com/example/order-processing-api",
//                              "createdAt": "2025-08-06T17:45:00"
//                            }
//                          ]
//                        }
//                        """
//                                    )
//                            )
//                    )
//            }
//    )
//    @GetMapping("/projects/{projectId}/repos")
//    public ResponseEntity<ProjectRepositoriesResponse> getProjectRepositories(
//            @PathVariable Long projectId
//    ) {
//        List<ProjectRepositoryResponse> repositories =
//                projectService.getRepositoriesByProjectId(projectId);
//        return ResponseEntity.ok(new ProjectRepositoriesResponse(repositories));
//    }


    @DeleteMapping("/repos/{repoId}")
    @Operation(summary = "리포지토리 연결 해제", description = "프로젝트에서 특정 GitHub 리포지토리 연결을 해제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "리포지토리 연결 해제 완료"),
    })
    public ResponseEntity<Void> unlinkRepository(
            @Parameter(description = "리포지토리 ID") @PathVariable Long repoId
    ) {
        projectService.deleteRepositoryById(repoId);
        return ResponseEntity.noContent().build();
    }

}
