package com.codaily.project.service;

import com.codaily.auth.entity.User;
import com.codaily.management.entity.DaysOfWeek;
import com.codaily.management.entity.Schedule;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.mypage.dto.ProjectUpdateRequest;
import com.codaily.project.dto.FeatureItemReduceItem;
import com.codaily.project.dto.FeatureItemReduceResponse;
import com.codaily.project.dto.ProjectCreateRequest;
import com.codaily.project.dto.ProjectRepositoryResponse;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.ProjectRepositories;
import com.codaily.project.entity.Specification;
import com.codaily.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final FeatureItemServiceImpl featureItemService;
    private final ProjectRepository projectRepository;
    private final ScheduleRepository scheduleRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final ProjectRepositoriesRepository repository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;


    public void saveRepositoryForProject(Long projectId, String repoName, String repoUrl) {
        ProjectRepositories entity = new ProjectRepositories();
        Project project = projectRepository.getProjectByProjectId(projectId);
        entity.setProject(project);
        entity.setRepoName(repoName);
        entity.setRepoUrl(repoUrl);
        entity.setCreatedAt(LocalDateTime.now());

        repository.save(entity);
    }

//    @Override
//    public List<ProjectRepositoryResponse> getRepositoriesByProjectId(Long projectId) {
//        List<ProjectRepositories> entities = repository.findByProjectId(projectId);
//        return entities.stream()
//                .map(repo -> ProjectRepositoryResponse.builder()
//                        .repoId(repo.getRepoId())
//                        .repoName(repo.getRepoName())
//                        .repoUrl(repo.getRepoUrl())
//                        .createdAt(repo.getCreatedAt())
//                        .build())
//                .toList();
//    }

    @Override
    public void deleteRepositoryById(Long repoId) {
        if (!repository.existsById(repoId)) {
            throw new IllegalArgumentException("해당 리포지토리가 존재하지 않습니다.");
        }
        repository.deleteById(repoId);
    }

    @Override
    @Transactional
    public Project createProject(ProjectCreateRequest request, User user) {
        Specification spec = specificationRepository.save(
                Specification.builder()
                        .title("자동 생성 중")
                        .content("")
                        .format("json")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        Project project = Project.builder()
                .user(user)
                .title("프로젝트 생성 중")
                .description("프로젝트 생성 중입니다.")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Project.ProjectStatus.TODO)
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
                        .dateName(entry.getKey())
                        .hours(entry.getValue())
                        .build())
                .toList();
        daysOfWeekRepository.saveAll(days);
        return project;
    }

    @Override
    @Transactional
    public int calculateTotalUserAvailableHours(Long projectId) {
        List<Schedule> schedules = scheduleRepository.findAllByProject_ProjectId(projectId);
        List<DaysOfWeek> daysOfWeeks = daysOfWeekRepository.findAllByProject_ProjectId(projectId);

        Map<DayOfWeek, Integer> hoursByDay = new HashMap<>();
        for (DaysOfWeek dow : daysOfWeeks) {
            DayOfWeek day = DayOfWeek.valueOf(dow.getDateName().toUpperCase());
            hoursByDay.put(day, dow.getHours());
        }

        int totalHours = 0;
        for (Schedule schedule : schedules) {
            DayOfWeek day = schedule.getScheduledDate().getDayOfWeek();
            totalHours += hoursByDay.getOrDefault(day, 0);
        }

        return totalHours;
    }

    @Override
    @Transactional
    public FeatureItemReduceResponse reduceFeatureItemsIfNeeded(Long projectId, Long specId) {
        int totalEstimated = featureItemRepository.getTotalEstimatedTimeBySpecId(specId);
        int totalAvailable = calculateTotalUserAvailableHours(projectId);

        List<FeatureItem> items = featureItemRepository.findAllBySpecification_SpecId(specId);

        List<FeatureItem> sorted = items.stream()
                .filter(item -> item.getPriorityLevel() != null)
                .sorted(Comparator
                        .comparingInt(FeatureItem::getPriorityLevel)
                        .thenComparing(Comparator.comparingDouble(FeatureItem::getEstimatedTime).reversed()))
                .toList();

        List<FeatureItemReduceItem> resultDtos = new ArrayList<>();
        double accumulated = 0;
        int reducedCount = 0, keptCount = 0;

        for (FeatureItem item : sorted) {
            boolean reduced;
            if (accumulated + item.getEstimatedTime() <= totalAvailable) {
                reduced = false;
                accumulated += item.getEstimatedTime();
                item.setIsReduced(false);
                keptCount++;
            } else {
                reduced = true;
                item.setIsReduced(true);
                reducedCount++;
            }

            resultDtos.add(FeatureItemReduceItem.builder()
                    .featureId(item.getFeatureId())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .estimatedTime(item.getEstimatedTime())
                    .priorityLevel(item.getPriorityLevel())
                    .isReduced(reduced)
                    .build());
        }

        return FeatureItemReduceResponse.builder()
                .totalEstimatedTime(totalEstimated)
                .totalAvailableTime(totalAvailable)
                .reducedCount(reducedCount)
                .keptCount(keptCount)
                .features(resultDtos)
                .build();
    }

    @Override
    @Transactional
    public void updateProject(Long projectId, ProjectUpdateRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        boolean scheduleChanged = isScheduleChanged(project, request);
        boolean daysOfWeekChanged = isDaysOfWeekChanged(project, request);
        boolean dateChanged = isDateChanged(project, request);

        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getScheduledDates() != null) {
            updateSchedules(project, request.getScheduledDates());
        }
        if (request.getDaysOfWeek() != null) {
            updateDaysOfWeek(project, request.getDaysOfWeek());
        }
        if (scheduleChanged || daysOfWeekChanged || dateChanged) {
            featureItemService.rescheduleProject(projectId);
        }
    }

    private void updateDaysOfWeek(Project project, List<ProjectUpdateRequest.DaysOfWeekRequest> daysOfWeek) {
        daysOfWeekRepository.deleteByProject(project);

        // 새로운 요일별 시간 생성
        List<DaysOfWeek> newDaysOfWeek = daysOfWeek.stream()
                .map(request -> DaysOfWeek.builder()
                        .project(project)
                        .dateName(request.getDateName())
                        .hours(request.getHours())
                        .build())
                .collect(Collectors.toList());

        daysOfWeekRepository.saveAll(newDaysOfWeek);
    }

    private void updateSchedules(Project project, List<LocalDate> scheduledDates) {
        scheduleRepository.deleteByProject(project);

        // 새로운 스케줄 생성
        List<Schedule> newSchedules = scheduledDates.stream()
                .map(date -> Schedule.builder()
                        .project(project)
                        .scheduledDate(date)
                        .build())
                .collect(Collectors.toList());

        scheduleRepository.saveAll(newSchedules);
    }



    private boolean isDateChanged(Project project, ProjectUpdateRequest request) {
        boolean startDateChanged = false;
        boolean endDateChanged = false;

        if (request.getStartDate() != null) {
            startDateChanged = !Objects.equals(project.getStartDate(), request.getStartDate());
        }

        if (request.getEndDate() != null) {
            endDateChanged = !Objects.equals(project.getEndDate(), request.getEndDate());
        }

        return startDateChanged || endDateChanged;
    }

    private boolean isDaysOfWeekChanged(Project project, ProjectUpdateRequest request) {
        if (request.getDaysOfWeek() == null) {
            return false;
        }

        // 현재 요일별 시간과 요청된 요일별 시간 비교
        List<DaysOfWeek> currentDaysOfWeek = daysOfWeekRepository.findByProject(project);

        if (currentDaysOfWeek.size() != request.getDaysOfWeek().size()) {
            return true;
        }

        // 각 요일별로 시간이 다른지 확인
        Map<String, Integer> currentMap = currentDaysOfWeek.stream()
                .collect(Collectors.toMap(DaysOfWeek::getDateName, DaysOfWeek::getHours));

        for (ProjectUpdateRequest.DaysOfWeekRequest requestDay : request.getDaysOfWeek()) {
            Integer currentHours = currentMap.get(requestDay.getDateName());
            if (currentHours == null || !currentHours.equals(requestDay.getHours())) {
                return true;
            }
        }

        return false;
    }

    private boolean isScheduleChanged(Project project, ProjectUpdateRequest request) {
        if (request.getScheduledDates() == null) {
            return false;
        }

        // 현재 스케줄과 요청된 스케줄 비교
        List<LocalDate> currentDates = scheduleRepository.findByProject(project)
                .stream()
                .map(Schedule::getScheduledDate)
                .sorted()
                .collect(Collectors.toList());

        List<LocalDate> requestDates = request.getScheduledDates()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        return !currentDates.equals(requestDates);
    }

    @Transactional
    public void updateProjectAndSpec(Long projectId, Long specId,
                                     String projectTitle, String projectDescription, String specTitle) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        Specification spec = specificationRepository.findById(specId)
                .orElseThrow(() -> new IllegalArgumentException("Specification not found: " + specId));

        // 프로젝트 정보 업데이트
        project.setTitle(projectTitle);
        project.setDescription(projectDescription);
        project.setUpdatedAt(LocalDateTime.now());

        // 명세서 정보 업데이트
        spec.setTitle(specTitle);
        spec.setUpdatedAt(LocalDateTime.now());
    }


    // 추후 삭제 예정
    @Override
    public Project findById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로젝트 없음"));
    }
}
