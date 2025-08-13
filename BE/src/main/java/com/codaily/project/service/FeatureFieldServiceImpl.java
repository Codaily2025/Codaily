package com.codaily.project.service;

import com.codaily.management.entity.FeatureItemSchedule;
import com.codaily.management.repository.FeatureItemSchedulesRepository;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.FeatureItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFieldServiceImpl implements FeatureFieldService{
    private final FeatureItemRepository featureItemRepository;
    private final FeatureItemSchedulesRepository featureItemSchedulesRepository;
    private final AsyncScheduleService asyncScheduleService;

    @Override
    public List<String> getFieldTabs(Long projectId) {
        return featureItemRepository.findDistinctFieldsByProjectId(projectId);
    }

    @Override
    public Map<String, List<FeatureItem>> getFeaturesByFieldAndStatus(Long projectId, String field) {
        List<FeatureItem> features = featureItemRepository.findByProjectIdAndField(projectId, field);

        Map<String, List<FeatureItem>> statusGroups = new LinkedHashMap<>();
        statusGroups.put("TODO", new ArrayList<>());
        statusGroups.put("IN_PROGRESS", new ArrayList<>());
        statusGroups.put("DONE", new ArrayList<>());

        for(FeatureItem feature : features){
            String status = feature.getStatus() != null ? feature.getStatus() : "TODO";
            statusGroups.computeIfAbsent(status, k -> new ArrayList<>()).add(feature);
        }
        return statusGroups;
    }

    @Override
    @Transactional
    public FeatureItem updateFeatureStatus(Long featureId, String newStatus) {
        FeatureItem feature = featureItemRepository.findByFeatureId(featureId)
                .orElseThrow(() -> new RuntimeException("해당 기능은 존재하지 않습니다."));

        String oldStatus = feature.getStatus();
        feature.setStatus(newStatus);
        FeatureItem savedFeature = featureItemRepository.save(feature);

        if("DONE".equals(newStatus) && !"DONE".equals(oldStatus)) {
            List<FeatureItemSchedule> schedules = featureItemSchedulesRepository.findByFeatureItem_FeatureIdOrderByScheduleDateAsc(featureId);

            if (!schedules.isEmpty()) {
                LocalDate lastScheduledDate = schedules.get(schedules.size() - 1).getScheduleDate();
                LocalDate today = LocalDate.now();

                // 예정된 마지막 날보다 일찍 완료된 경우에만 재스케줄링
                if (today.isBefore(lastScheduledDate)) {
                    featureItemSchedulesRepository.deleteByFeatureItem_FeatureIdAndScheduleDateAfter(
                            featureId, today);

                    asyncScheduleService.rescheduleProjectAsync(feature.getProjectId())
                            .whenComplete((result, throwable) -> {
                                if (throwable != null) {
                                    log.error("기능 완료 후 일정 재조정 실패 - 기능: {}, 프로젝트: {}",
                                            featureId, feature.getProjectId(), throwable);
                                } else {
                                    log.info("기능 완료 후 일정 재조정 성공 - 기능: {}, 프로젝트: {}",
                                            featureId, feature.getProjectId());
                                }
                            });
                }
            }
        }
        return savedFeature;
    }
}
