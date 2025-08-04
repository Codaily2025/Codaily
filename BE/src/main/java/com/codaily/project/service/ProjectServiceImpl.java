package com.codaily.project.service;

import com.codaily.auth.entity.User;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.entity.*;
import com.codaily.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ScheduleRepository scheduleRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final ProjectRepositoriesRepository repository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;


    public void saveRepositoryForProject(Long projectId, String repoName, String repoUrl) {
        ProjectRepositories entity = new ProjectRepositories();
        entity.setProjectId(projectId);
        entity.setRepoName(repoName);
        entity.setRepoUrl(repoUrl);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);
    }

    @Override
    public List<ProjectRepositoryResponse> getRepositoriesByProjectId(Long projectId) {
        List<ProjectRepositories> entities = repository.findByProjectId(projectId);
        return entities.stream()
                .map(repo -> ProjectRepositoryResponse.builder()
                        .repoId(repo.getRepoId())
                        .repoName(repo.getRepoName())
                        .repoUrl(repo.getRepoUrl())
                        .createdAt(repo.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public void deleteRepositoryById(Long repoId) {
        if (!repository.existsById(repoId)) {
            throw new IllegalArgumentException("해당 리포지토리가 존재하지 않습니다.");
        }
        repository.deleteById(repoId);
    }

    @Override
    @Transactional
    public void createProject(ProjectCreateRequest request, User user) {
        Specification spec = specificationRepository.save(
                Specification.builder()
                        .user(user)
                        .title("자동 생성 중")
                        .content("")
                        .format("json")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        Project project = Project.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status("TODO")
                .specification(spec)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Project savedProject = projectRepository.save(project);

        List<Schedule> schedules = request.getAvailableDates().stream()
                .map(date -> Schedule.builder()
                        .project(savedProject)
                        .scheduledDate(date)
                        .build())
                .toList();
        scheduleRepository.saveAll(schedules);

        List<DaysOfWeek> days = request.getWorkingHours().entrySet().stream()
                .map(entry -> DaysOfWeek.builder()
                        .project(savedProject)
                        .dayName(entry.getKey())
                        .hours(entry.getValue())
                        .build())
                .toList();
        daysOfWeekRepository.saveAll(days);
    }
}
