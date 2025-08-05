package com.codaily.mypage.controller;

import com.codaily.mypage.dto.ProjectListResponse;
import com.codaily.mypage.dto.ProjectStatusResponse;
import com.codaily.mypage.service.MyPageProjectServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/projects")
public class MyPageProjectController {

    private final MyPageProjectServiceImpl myPageProjectService;

    @GetMapping
    @Operation(summary = "프로젝트 목록 조회", description = "해당 사용자의 프로젝트 전체 표시")
    public ResponseEntity<List<ProjectListResponse>> getProjectList(
            @PathVariable Long userId
    ){
        List<ProjectListResponse> projects = myPageProjectService.getProjectList(userId);
        return ResponseEntity.ok(projects);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary ="프로젝트 삭제")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long userId,
            @PathVariable Long projectId
    ){
        myPageProjectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{projectId}/complete")
    @Operation(summary = "프로젝트 완료 처리")
    public ResponseEntity<ProjectStatusResponse> completeProject(
            @PathVariable Long userId, @PathVariable Long projectId
    ){
        ProjectStatusResponse response = myPageProjectService.completeProject(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "프로젝트 상태 별 검색", description = "전체/진행중인 프로젝트/완료된 프로젝트")
    public ResponseEntity<List<ProjectListResponse>> searchProjectByStatus(
            @PathVariable Long userId,
            @RequestParam String status
    ){
        List<ProjectListResponse> projects = myPageProjectService.searchProjectsByStatus(userId, status);
        return ResponseEntity.ok(projects);
    }
}
