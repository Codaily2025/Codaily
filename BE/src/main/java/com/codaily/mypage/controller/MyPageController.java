package com.codaily.mypage.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.UserService;
import com.codaily.mypage.dto.*;
import com.codaily.mypage.service.MyPageService;
import com.codaily.project.entity.Project;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MyPageController {

    private final MyPageService myPageService;
    private final UserService userService;
    private final ProjectService projectService;

    @GetMapping("/projects")
    @Operation(summary = "프로젝트 목록 조회", description = "해당 사용자의 프로젝트 전체 표시")
    public ResponseEntity<List<ProjectListResponse>> getProjectList(
            @AuthenticationPrincipal PrincipalDetails userDetails
            ){
        Long userId = userDetails.getUserId();
        List<ProjectListResponse> projects = myPageService.getProjectList(userId);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/projects/{projectId}")
    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트 제목, 기간, 요일별 작업 가능 시간, 작업 가능 날짜, 연결된 레포지토리를 보여줍니다.")
    public ResponseEntity<ProjectDetailResponse> getProjectDetail(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @PathVariable Long projectId
    ){
        ProjectDetailResponse project = myPageService.getProjectDetail(projectId);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary ="프로젝트 삭제")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        Long userId = userDetails.getUserId();

        if (!projectService.isProjectOwner(userId, projectId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        myPageService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/projects/{projectId}/complete")
    @Operation(summary = "프로젝트 완료 처리")
    public ResponseEntity<ProjectStatusResponse> completeProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        Long userId = userDetails.getUserId();

        if (!projectService.isProjectOwner(userId, projectId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ProjectStatusResponse response = myPageService.completeProject(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/search")
    @Operation(summary = "프로젝트 상태 별 검색", description = "전체/진행중인 프로젝트/완료된 프로젝트")
    public ResponseEntity<List<ProjectListResponse>> searchProjectByStatus(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestParam Project.ProjectStatus status
    ){
        Long userId = userDetails.getUserId();
        List<ProjectListResponse> projects = myPageService.searchProjectsByStatus(userId, status);
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
    @Operation(summary = "닉네임 수정", description = "마이페이지 & 추가정보 입력에서 닉네임 수정")
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
            ) throws AccessDeniedException {
        Long userId = userDetails.getUserId();

        if (!projectService.isProjectOwner(userId, projectId)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        projectService.updateProject(projectId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload-profile-image")
    @Operation(summary = "프로필 사진 업로드", description = "첫 로그인 후 추가 정보 등록 시 or 마이페이지에서 프로필 사진 업로드 & 수정 가능")
    public ResponseEntity<ProfileImageUploadResponse> uploadProfileImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = myPageService.uploadProfileImage(
                principalDetails.getUser().getUserId(), file);

        ProfileImageUploadResponse response = ProfileImageUploadResponse.builder()
                .message("프로필 이미지가 업로드되었습니다.")
                .imageUrl(imageUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile-image")
    @Operation(summary = "프로필 사진 조회", description = "현재 로그인한 사용자의 프로필 사진 URL을 조회합니다.")
    public ResponseEntity<ProfileImageResponse> getProfileImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        String imageUrl = myPageService.getProfileImage(
                principalDetails.getUser().getUserId());

        ProfileImageResponse response = ProfileImageResponse.builder()
                .imageUrl(imageUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile-image")
    @Operation(summary = "프로필 이미지 삭제")
    public ResponseEntity<Map<String, String>> deleteProfileImage(
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        myPageService.deleteProfileImage(
                principalDetails.getUser().getUserId());

        Map<String, String> response = new HashMap<>();
        response.put("message", "프로필 이미지가 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/github-account")
    @Operation(summary = "등록된 깃허브 아이디 조회")
    public ResponseEntity<GithubAccountResponse> getGithubAccount(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        myPageService.getGithubAccount(principalDetails.getUser().getUserId());

        GithubAccountResponse response = GithubAccountResponse.builder()
                .githubId(principalDetails.getUser().getGithubAccount())
                .build();

        return ResponseEntity.ok(response);
    }

    // 작성자: yeongenn - 최초 로그인 시 추가 정보 입력 API
    @PutMapping("/user/additional-info")
    @Operation(summary = "사용자 추가 정보 업데이트",
            description = "최초 로그인 후 닉네임, GitHub 계정, 프로필 이미지를 한 번에 업데이트")
    public ResponseEntity<Map<String, String>> updateAdditionalInfo(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("nickname") String nickname,
//            @RequestParam("githubAccount") String githubAccount,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        Long userId = principalDetails.getUser().getUserId();

        try {
            // 닉네임 업데이트
            userService.updateUserNickname(userId, nickname);

            // GitHub 계정 업데이트
//            myPageService.updateGithubAccount(userId, githubAccount);

            // 프로필 이미지 업로드 (있는 경우에만)
            if (profileImage != null && !profileImage.isEmpty()) {
                myPageService.uploadProfileImage(userId, profileImage);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "추가 정보가 성공적으로 업데이트되었습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "추가 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/user")
    @Operation(summary = "회원 탈퇴")
    public ResponseEntity<Map<String, String>> deleteUser(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ){
        log.info("userDetail user: {}", userDetails.getUser());
        userService.deleteUser(userDetails.getUser().getUserId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "회원 탈퇴가 완료되었습니다.");

        return ResponseEntity.ok(response);
    }
}
