package com.codaily.project.service;

import com.codaily.project.entity.FeatureItem;

import java.time.LocalDate;

public interface ScheduleService {
    void rescheduleProject(Long projectId);
    void scheduleProjectInitially(Long projectId);
    void rescheduleFromFeatureCreate(Long projectId, FeatureItem newFeature);
    void rescheduleFromFeatureUpdate(Long projectId, FeatureItem updatedFeature, Integer oldPriorityLevel, Double oldEstimatedTime);
    void rescheduleFromFeatureDelete(Long projectId, FeatureItem deletedFeature);
    void updateDailyStatus();

    void updateInProgressEstimatedTime(Long projectId);
    void handleOverdueFeatures(Long projectId);
    void startTodayFeatures(Long projectId, LocalDate today);
}
