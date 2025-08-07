package com.codaily.mypage.service;

import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.mypage.dto.ProjectListResponse;
import com.codaily.mypage.dto.ProjectStatusResponse;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageServiceImpl implements MyPageService {

    private final FeatureItemSchedulesRepository schedulesRepository;
    private final FeatureItemRepository featureItemRepository;
    private final ProjectRepository projectRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final ScheduleRepository scheduleRepository;


    @Override
    public List<ProjectListResponse> getProjectList(Long userId) {
        List<Project> projects = projectRepository.findByUser_UserId(userId);

        return projects.stream()
                .map(this::converToList)
                .collect(Collectors.toList());
    }

    @Override
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
    public List<ProjectListResponse> searchProjectsByStatus(Long userId, String status) {
        List<Project> projects = projectRepository.findByStatusAndUser_UserIdOrderByCreatedAtDesc(status, userId);

        return projects.stream()
                .map(this::converToList)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isProjectOwner(Long projectId, Long userId) {
        return projectRepository.findById(projectId)
                .map(project -> project.getUserId().equals(userId))
                .orElse(false);
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
