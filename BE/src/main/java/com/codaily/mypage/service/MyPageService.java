package com.codaily.mypage.service;

import com.codaily.mypage.dto.ProjectListResponse;
import com.codaily.mypage.dto.ProjectStatusResponse;
import com.codaily.project.entity.Project;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyPageService {
    List<ProjectListResponse> getProjectList(Long userId);

    void deleteProject(Long projectId);

    ProjectStatusResponse completeProject(Long projectId);

    List<ProjectListResponse> searchProjectsByStatus(Long userId, Project.ProjectStatus status);

    String uploadProfileImage(Long userId, MultipartFile file);

    void deleteProfileImage(Long userId);
}
