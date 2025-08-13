package com.codaily.mypage.service;

import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.common.file.service.FileStorageService;
import com.codaily.common.file.service.FileStorageServiceImpl;
import com.codaily.management.entity.DaysOfWeek;
import com.codaily.management.entity.Schedule;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.mypage.dto.*;
import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.ProjectRepositories;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
        List<Project> projectsWithDays = projectRepository.findByUserIdWithDays(userId);
        List<Project> projectsWithSchedules = projectRepository.findByUserIdWithSchedules(userId);

        Map<Long, Project> projectMap = projectsWithDays.stream()
                .collect(Collectors.toMap(Project::getProjectId, Function.identity()));

        projectsWithSchedules.forEach(projectWithSchedule -> {
            Project existingProject = projectMap.get(projectWithSchedule.getProjectId());
            if (existingProject != null) {
                existingProject.setSchedules(projectWithSchedule.getSchedules());
            }
        });

        return projectMap.values().stream()
                .map(this::convertToList)
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
                .map(this::convertToList)
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
    public String getProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return user.getProfileImage();
    }

    @Override
    public String getGithubAccount(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return user.getGithubAccount();
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

    // 작성자: yeongenn - GitHub 계정 업데이트 구현
    @Override
    @Transactional
    public void updateGithubAccount(Long userId, String githubAccount) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // GitHub 계정명 중복 체크 (해당 사용자가 아닌 다른 사용자가 사용 중인지 확인)
        if (userRepository.existsByGithubAccount(githubAccount)) {
            User existingUser = userRepository.findByNickname(githubAccount).orElse(null);
            if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                throw new IllegalArgumentException("이미 사용 중인 GitHub 계정입니다.");
            }
        }

        user.setGithubAccount(githubAccount);
        userRepository.save(user);
    }

    @Override
    public ProjectDetailResponse getProjectDetail(Long projectId) {
        Project project = projectRepository.getProjectByProjectId(projectId);

        if(project == null){
            throw new IllegalArgumentException("해당 프로젝트를 찾을 수 없습니다.");
        }

        Project projectWithDays = projectRepository.findByIdWithDays(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Project projectWithSchedules = projectRepository.findByIdWithSchedules(projectId)
                .orElse(null);

        Project projectWithRepositories = projectRepository.findByIdWithRepositories(projectId)
                .orElse(null);

        if (projectWithSchedules != null) {
            projectWithDays.setSchedules(projectWithSchedules.getSchedules());
        }

        if (projectWithRepositories != null) {
            projectWithDays.setProjectRepositories(projectWithRepositories.getProjectRepositories());
        }

        return ProjectDetailResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus() != null ? project.getStatus().name() : null)
                .daysOfWeeks(convertDaysOfWeekToDto(project.getDaysOfWeek()))
                .schedules(convertSchedulesToDto(project.getSchedules()))
                .repositories(convertRepositoriesToDto(project.getProjectRepositories()))
                .build();
    }

    private List<DaysOfWeekResponse> convertDaysOfWeekToDto(List<DaysOfWeek> daysOfWeeks) {
        if (daysOfWeeks == null) return new ArrayList<>();

        return daysOfWeeks.stream()
                .map(day -> DaysOfWeekResponse.builder()
                        .dayId(day.getDaysId())
                        .dateName(day.getDateName())
                        .hours(day.getHours())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ScheduleResponse> convertSchedulesToDto(List<Schedule> schedules) {
        if (schedules == null) return new ArrayList<>();

        return schedules.stream()
                .map(schedule -> ScheduleResponse.builder()
                        .scheduleId(schedule.getScheduleId())
                        .scheduledDate(schedule.getScheduledDate())
                        .build())
                .collect(Collectors.toList());
    }

    private ProjectListResponse convertToList(Project project){
        return ProjectListResponse.builder()
                .projectId(project.getProjectId())
                .title(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().toString())
                .daysOfWeeks(convertDaysOfWeekToDto(project.getDaysOfWeek()))
                .schedules(convertSchedulesToDto(project.getSchedules()))
                .build();
    }

    private List<ProjectRepositoryResponse> convertRepositoriesToDto(List<ProjectRepositories> projectRepositories) {
        if (projectRepositories == null) return new ArrayList<>();

        return projectRepositories.stream()
                .map(pr -> ProjectRepositoryResponse.builder()
                        .repoId(pr.getRepoId())
                        .repoName(pr.getRepoName())
                        .repoUrl(pr.getRepoUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
