package com.codaily.mypage.service;

import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.common.file.service.FileStorageService;
import com.codaily.common.file.service.FileStorageServiceImpl;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.mypage.dto.ProjectListResponse;
import com.codaily.mypage.dto.ProjectStatusResponse;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageServiceImpl implements MyPageService {

    private final FeatureItemSchedulesRepository schedulesRepository;
    private final FeatureItemRepository featureItemRepository;
    private final ProjectRepository projectRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;


    @Override
    public List<ProjectListResponse> getProjectList(Long userId) {
        List<Project> projects = projectRepository.findByUser_UserId(userId);

        return projects.stream()
                .map(this::converToList)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        if (!projectRepository.existsByProjectId(projectId)) {
            throw new IllegalArgumentException("프로젝트를 찾을 수 없습니다.");
        }

        List<Long> featureItems = featureItemRepository.findFeatureIdByProject_ProjectId(projectId);
        if(!featureItems.isEmpty()){
            schedulesRepository.deleteByFeatureItemFeatureIdIn(featureItems);
            featureItemRepository.deleteAllById(featureItems);
        }
        daysOfWeekRepository.deleteByProjectId(projectId);
        scheduleRepository.deleteByProjectId(projectId);
        projectRepository.deleteByProjectId(projectId);
    }

    @Override
    @Transactional
    public ProjectStatusResponse completeProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        if (Project.ProjectStatus.COMPLETED.equals(project.getStatus())) {
            throw new IllegalStateException("이미 완료된 프로젝트입니다.");
        }
        //e
        project.setStatus(Project.ProjectStatus.valueOf("DONE"));

        project.setStatus(Project.ProjectStatus.COMPLETED);
        projectRepository.save(project);

        return ProjectStatusResponse.builder()
                .projectId(project.getProjectId())
                .status(project.getStatus().toString())
                .build();
    }

    @Override
    public List<ProjectListResponse> searchProjectsByStatus(Long userId, Project.ProjectStatus status) {
        List<Project> projects = projectRepository.findByStatusAndUser_UserIdOrderByCreatedAtDesc(status, userId);

        return projects.stream()
                .map(this::converToList)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
       if(file == null || file.isEmpty()) return null;

       User user = userRepository.findByUserId(userId)
               .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            // 기존 프로필 이미지 삭제
            if (user.getProfileImage() != null) {
                fileStorageService.deleteFile(user.getProfileImage());
            }

            // 새 이미지 업로드
            String imageUrl = fileStorageService.uploadFile(file, "profiles");
            user.setProfileImage(imageUrl);
            userRepository.save(user);

            return imageUrl;

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getProfileImage() != null) {
            fileStorageService.deleteFile(user.getProfileImage());
            user.setProfileImage(null);
            userRepository.save(user);
        }
    }

    private ProjectListResponse converToList(Project project){
        return ProjectListResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().toString())
                .build();
    }
}
