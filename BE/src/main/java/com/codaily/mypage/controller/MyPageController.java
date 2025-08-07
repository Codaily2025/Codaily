package com.codaily.mypage.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.UserServiceImpl;
import com.codaily.mypage.dto.NicknameUpdateRequest;
import com.codaily.mypage.dto.ProjectListResponse;
import com.codaily.mypage.dto.ProjectStatusResponse;
import com.codaily.mypage.dto.ProjectUpdateRequest;
import com.codaily.mypage.service.MyPageServiceImpl;
import com.codaily.project.service.ProjectServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MyPageController {

    private final MyPageServiceImpl myPageProjectService;
    private final UserServiceImpl userService;
    private final ProjectServiceImpl projectService;

    @GetMapping("/projects")
    @Operation(summary = "프로젝트 목록 조회", description = "해당 사용자의 프로젝트 전체 표시")
    public ResponseEntity<List<ProjectListResponse>> getProjectList(
            @AuthenticationPrincipal PrincipalDetails userDetails
            ){
        Long userId = userDetails.getUserId();
        List<ProjectListResponse> projects = myPageProjectService.getProjectList(userId);
        return ResponseEntity.ok(projects);
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary ="프로젝트 삭제")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        Long userId = userDetails.getUserId();

        if (!myPageProjectService.isProjectOwner(projectId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        myPageProjectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/projects/{projectId}/complete")
    @Operation(summary = "프로젝트 완료 처리")
    public ResponseEntity<ProjectStatusResponse> completeProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        Long userId = userDetails.getUserId();

        if (!myPageProjectService.isProjectOwner(projectId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProjectStatusResponse response = myPageProjectService.completeProject(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/search")
    @Operation(summary = "프로젝트 상태 별 검색", description = "전체/진행중인 프로젝트/완료된 프로젝트")
    public ResponseEntity<List<ProjectListResponse>> searchProjectByStatus(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestParam String status
    ){
        Long userId = userDetails.getUserId();
        List<ProjectListResponse> projects = myPageProjectService.searchProjectsByStatus(userId, status);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/nickname")
    @Operation(summary = "닉네임 조회", description = "마이페이지에 닉네임 표시")
    public ResponseEntity<Map<String, String>> getUserNickname(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        String nickname = userService.getUserNickname(userId);
        Map<String, String> response = new HashMap<>();
        response.put("nickname", nickname);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/nickname")
    @Operation(summary = "닉네임 수정", description = "마이페이지에서 닉네임 수정")
    public ResponseEntity<Void> modifyUserNickname(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestBody NicknameUpdateRequest request) {

        Long userId = userDetails.getUserId();
        userService.updateUserNickname(userId, request.getNickname());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("projects/{projectId}")
    @Operation(summary = "프로젝트 수정", description = "프로젝트 기본 정보, 스케줄, 요일별 작업시간 수정")
    public ResponseEntity<Void> updateProject(
            @PathVariable Long projectId,
            @RequestBody ProjectUpdateRequest request,
            @AuthenticationPrincipal PrincipalDetails userDetails
            ) {
        Long userId = userDetails.getUserId();

        if (!myPageProjectService.isProjectOwner(projectId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        projectService.updateProject(projectId, request);
        return ResponseEntity.ok().build();
    }
}
