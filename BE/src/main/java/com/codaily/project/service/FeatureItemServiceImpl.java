package com.codaily.project.service;

import com.codaily.global.exception.ProjectNotFoundException;
import com.codaily.management.dto.FeatureScheduleResponse;
import com.codaily.management.entity.DaysOfWeek;
import com.codaily.management.entity.FeatureItemSchedule;
import com.codaily.management.entity.Schedule;
import com.codaily.management.repository.DaysOfWeekRepository;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.project.dto.FeatureItemCreate;
import com.codaily.project.dto.FeatureItemResponse;
import com.codaily.project.dto.FeatureItemUpdate;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.exception.FeatureNotFoundException;
import com.codaily.project.exception.SpecificationNotFoundException;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.repository.ScheduleRepository;
import com.codaily.project.repository.SpecificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeatureItemServiceImpl implements FeatureItemService {

    private final ProjectRepository projectRepository;
    private final SpecificationRepository specificationRepository;
    private final FeatureItemRepository featureItemRepository;
    private final FeatureItemSchedulesRepository featureItemScheduleRepository;
    private final DaysOfWeekRepository daysOfWeekRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public FeatureItemResponse createFeature(Long projectId, FeatureItemCreate featureItem) {
        if (projectId == null || featureItem == null) {
            throw new IllegalArgumentException("프로젝트 ID와 생성 정보는 필수입니다.");
        }
        if (featureItem.getTitle() == null || featureItem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("기능 제목은 필수입니다.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        FeatureItem feature = FeatureItem.builder()
                .title(featureItem.getTitle().trim())
                .description(featureItem.getDescription())
                .field(featureItem.getField())
                .category(featureItem.getCategory())
                .priorityLevel(featureItem.getPriorityLevel())
                .estimatedTime(featureItem.getEstimatedTime())
                .isCustom(true)
                .isSelected(true)
                .isReduced(false)
                .project(project)
                .build();

        // 스펙 설정
        if (featureItem.getSpecificationId() != null) {
            Specification specification = specificationRepository.findById(featureItem.getSpecificationId())
                    .orElseThrow(() -> new SpecificationNotFoundException(featureItem.getSpecificationId()));
            feature.setSpecification(specification);
        }

        // 부모 기능 설정
        if (featureItem.getParentFeatureId() != null) {
            FeatureItem parentFeature = featureItemRepository.findByProject_ProjectIdAndFeatureId(
                            projectId, featureItem.getParentFeatureId())
                    .orElseThrow(() -> new FeatureNotFoundException(featureItem.getParentFeatureId()));
            feature.setParentFeature(parentFeature);
        }

        FeatureItem savedFeature = featureItemRepository.save(feature);

        if (featureItem.getEstimatedTime() != null && featureItem.getEstimatedTime() > 0) {
            rescheduleProject(projectId);
        }

        log.info("기능 생성 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, savedFeature.getFeatureId());

        return convertToResponseDto(feature);
    }

    @Override
    public FeatureItemResponse getFeature(Long projectId, Long featureId) {
        if (projectId == null || featureId == null) {
            throw new IllegalArgumentException("프로젝트 ID와 기능 ID는 필수입니다.");
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));

        return convertToResponseDto(feature);
}

    @Override
    public FeatureItemResponse updateFeature(Long projectId, Long featureId, FeatureItemUpdate update) {
        if (projectId == null || featureId == null || update == null) {
            throw new IllegalArgumentException();
        }

        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));


        boolean needsRescheduling = false;

        if (update.getTitle() != null) {
            if (update.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("기능 제목은 비어있을 수 없습니다.");
            }
            feature.setTitle(update.getTitle().trim());
        }

        if (update.getDescription() != null) {
            feature.setDescription(update.getDescription());
        }
        if (update.getField() != null) {
            feature.setField(update.getField());
        }
        if (update.getCategory() != null) {
            feature.setCategory(update.getCategory());
        }
        if (update.getStatus() != null) {
            feature.setStatus(update.getStatus());
        }
        if (update.getPriorityLevel() != null) {
            Integer oldPriority = feature.getPriorityLevel();
            feature.setPriorityLevel(update.getPriorityLevel());
            if (!java.util.Objects.equals(oldPriority, update.getPriorityLevel())) {
                needsRescheduling = true;
            }
        }
        if (update.getEstimatedTime() != null) {
            if (update.getEstimatedTime() < 0) {
                throw new IllegalArgumentException("예상 시간은 0 이상이어야 합니다.");
            }
            Integer oldTime = feature.getEstimatedTime();
            feature.setEstimatedTime(update.getEstimatedTime());
            if (!java.util.Objects.equals(oldTime, update.getEstimatedTime())) {
                needsRescheduling = true;
            }
        }
        if (update.getIsReduced() != null) {
            feature.setIsReduced(update.getIsReduced());
        }

        if(needsRescheduling) rescheduleProject(projectId);

        log.info("기능 수정 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, featureId);
        return convertToResponseDto(feature);
    }

    @Override
    public void deleteFeature(Long projectId, Long featureId) {
        if (projectId == null || featureId == null) {
            throw new IllegalArgumentException("프로젝트 ID와 기능 ID는 필수입니다.");
        }

        FeatureItem feature = featureItemRepository.findByProject_ProjectIdAndFeatureId(projectId, featureId)
                .orElseThrow(() -> new FeatureNotFoundException(featureId));

        // 연관된 스케줄들 삭제
        if (featureItemScheduleRepository != null) {
            featureItemScheduleRepository.deleteByFeatureItemFeatureId(featureId);
            log.info("기능 관련 스케줄 삭제 완료 - 기능 ID: {}", featureId);
        }

        // 하위 기능들의 부모 참조 제거
        feature.getChildFeatures().forEach(child -> child.setParentFeature(null));

        featureItemRepository.delete(feature);
        rescheduleProject(projectId);
        log.info("기능 삭제 완료 - 프로젝트 ID: {}, 기능 ID: {}", projectId, featureId);
    }

    @Override
    public List<FeatureScheduleResponse> getFeatureSchedules(Long projectId, Long featureId) {
        return null;
    }

    @Override
    public List<FeatureScheduleResponse> getSchedulesByDate(Long projectId, LocalDate date) {
        return null;
    }

    @Override
    public void rescheduleProject(Long projectId) {
        // 재스케줄링 가능한 기능들을 우선순위 순으로 조회
        List<FeatureItem> features = getSchedulableFeatures(projectId);

        if (features.isEmpty()) {
            log.info("재스케줄링할 기능이 없습니다.");
            return;
        }

        // 기존 스케줄 삭제 (재스케줄링 대상만)
        deleteExistingSchedules(features);

        // 시작 날짜 결정
        LocalDate startDate = determineStartDate();

        // 우선순위 순으로 다시 스케줄링
        scheduleFeatures(features, startDate, projectId);

        log.info("프로젝트 재스케줄링 완료 - 기능 수: {}", features.size());
    }

    private void scheduleFeatures(List<FeatureItem> features, LocalDate startDate, Long projectId) {
        LocalDate currentDate = startDate;
        Map<String, Integer> weeklySchedule = getWeeklyScheduleMap(projectId);

        // PriorityQueue로 실시간 우선순위 관리
        PriorityQueue<FeatureItem> remainingFeatures = new PriorityQueue<>();
        remainingFeatures.addAll(features);

        while (!remainingFeatures.isEmpty()) {
            FeatureItem currentFeature = remainingFeatures.poll();
            currentDate = scheduleFeature(currentFeature, currentDate, weeklySchedule);
        }
    }

    private LocalDate scheduleFeature(FeatureItem feature, LocalDate startDate, Map<String, Integer> weeklySchedule) {
        int remainingHours = feature.getEstimatedTime();
        LocalDate currentDate = startDate;
        Long projectId = feature.getProject().getProjectId();

        int maxDays = 90;
        int dayCount = 0;

        while (remainingHours > 0 && dayCount < maxDays) {
            int availableHours = getAvailableHours(projectId, currentDate, weeklySchedule);

            if (availableHours > 0) {
                int allocatedHours = getAllocatedHours(projectId, currentDate);
                int freeHours = Math.max(0, availableHours - allocatedHours);

                if (freeHours > 0) {
                    int hoursToSchedule = Math.min(remainingHours, freeHours);

                    // 스케줄 생성
                    FeatureItemSchedule schedule = FeatureItemSchedule.builder()
                            .featureItem(feature)
                            .scheduleDate(currentDate)
                            .allocatedHours(hoursToSchedule)
                            .build();

                    featureItemScheduleRepository.save(schedule);
                    remainingHours -= hoursToSchedule;

                    // 현재 날짜에 시간이 남았다면 다음 기능도 같은 날 시작 가능
                    if (remainingHours == 0) {
                        int totalUsed = allocatedHours + hoursToSchedule;
                        if (totalUsed < availableHours) {
                            return currentDate; // 같은 날 반환
                        } else {
                            return getNextWorkingDay(projectId, currentDate, weeklySchedule);
                        }
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
            dayCount++;
        }


        if (remainingHours > 0) {
            log.warn("스케줄링 미완료 - 기능 ID: {}, 남은 시간: {}h", feature.getFeatureId(), remainingHours);
        }

        return currentDate;
    }

    private int getAvailableHours(Long projectId, LocalDate date, Map<String, Integer> weeklySchedule) {
        // 해당 날짜가 작업 가능한 날인지 확인
        List<Schedule> schedules = scheduleRepository.findByProject_ProjectIdAndScheduledDate(projectId, date);

        if (schedules.isEmpty()) {
            return 0; // 작업 불가능한 날
        }

        // 요일별 기본 작업 시간 반환
        String dayName = date.getDayOfWeek().toString();
        return weeklySchedule.getOrDefault(dayName, 0);
    }

    private int getAllocatedHours(Long projectId, LocalDate date) {
        List<FeatureItemSchedule> schedules = featureItemScheduleRepository
                .findByFeatureItem_Project_ProjectIdAndScheduleDate(projectId, date);

        return schedules.stream()
                .mapToInt(FeatureItemSchedule::getAllocatedHours)
                .sum();
    }

    private LocalDate getNextWorkingDay(Long projectId, LocalDate fromDate, Map<String, Integer> weeklySchedule) {
        LocalDate nextDate = fromDate.plusDays(1);

        for (int i = 0; i < 14; i++) {
            if (getAvailableHours(projectId, nextDate, weeklySchedule) > 0) {
                return nextDate;
            }
            nextDate = nextDate.plusDays(1);
        }

        return fromDate.plusDays(1);
    }

    private Map<String, Integer> getWeeklyScheduleMap(Long projectId) {
        List<DaysOfWeek> daysOfWeekList = daysOfWeekRepository.findByProject_ProjectId(projectId);

        return daysOfWeekList.stream()
                .collect(Collectors.toMap(
                        DaysOfWeek::getDateName,
                        DaysOfWeek::getHours,
                        (existing, replacement) -> existing
                ));
    }

    private LocalDate determineStartDate() {
        Optional<LocalDate> latestProgressDate = featureItemScheduleRepository
                .findLatestScheduleDateForProgressFeatures();

        if (latestProgressDate.isPresent()) {
            return latestProgressDate.get();
        } else {
            return LocalDate.now();
        }
    }

    private void deleteExistingSchedules(List<FeatureItem> features) {
        List<Long> featureIds = features.stream()
                .map(FeatureItem::getFeatureId)
                .collect(Collectors.toList());

        featureItemScheduleRepository.deleteByFeatureItemFeatureIdIn(featureIds);
        log.debug("기존 스케줄 삭제 완료 - 기능 수: {}", featureIds.size());
    }

    private List<FeatureItem> getSchedulableFeatures(Long projectId) {
        List<FeatureItem> allFeatures = featureItemRepository.findByProject_ProjectId(projectId);

        return allFeatures.stream()
                .filter(this::isSchedulable)
                .filter(feature -> feature.getEstimatedTime() != null && feature.getEstimatedTime() > 0)
                .collect(Collectors.toList());
    }

    private boolean isSchedulable(FeatureItem feature) {
        String status = feature.getStatus();

        if(status.equals("DONE") || status.equals("IN PROGRESS"))
            return false;

        return true;
    }

    private FeatureItemResponse convertToResponseDto(FeatureItem feature) {
        return FeatureItemResponse.builder()
                .featureId(feature.getFeatureId())
                .title(feature.getTitle())
                .description(feature.getDescription())
                .field(feature.getField())
                .category(feature.getCategory())
                .status(feature.getStatus())
                .priorityLevel(feature.getPriorityLevel())
                .estimatedTime(feature.getEstimatedTime())
                .isSelected(feature.getIsSelected())
                .isCustom(feature.getIsCustom())
                .isReduced(feature.getIsReduced())
                .projectId(feature.getProject().getProjectId())
                .specificationId(feature.getSpecification() != null ? feature.getSpecification().getSpecId() : null)
                .parentFeatureId(feature.getParentFeature() != null ? feature.getParentFeature().getFeatureId() : null)
                .build();
    }
}
