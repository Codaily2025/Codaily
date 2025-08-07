package com.codaily.mypage.service;

import com.codaily.mypage.dto.ProjectListResponse;
import com.codaily.mypage.dto.ProjectStatusResponse;

import java.util.List;

public interface MyPageService {
    List<ProjectListResponse> getProjectList(Long userId);

    void deleteProject(Long projectId);

    ProjectStatusResponse completeProject(Long projectId);

    List<ProjectListResponse> searchProjectsByStatus(Long userId, String status);

    boolean isProjectOwner(Long projectId, Long userId);
}
