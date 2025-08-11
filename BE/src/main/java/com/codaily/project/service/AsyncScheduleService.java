package com.codaily.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.codaily.project.entity.FeatureItem;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncScheduleService {

    private final ScheduleService scheduleService;

    @Async("scheduleTaskExecutor")
    public CompletableFuture<Void> rescheduleFromFeatureCreateAsync(Long projectId, FeatureItem newFeature) {
        try {
            log.info("비동기 기능 생성 재스케줄링 시작 - 프로젝트: {}, 기능: {}",
                    projectId, newFeature.getFeatureId());

            long startTime = System.currentTimeMillis();
            scheduleService.rescheduleFromFeatureCreate(projectId, newFeature);
            long endTime = System.currentTimeMillis();

            log.info("비동기 기능 생성 재스케줄링 완료 - 프로젝트: {}, 소요시간: {}ms",
                    projectId, (endTime - startTime));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("비동기 기능 생성 재스케줄링 실패 - 프로젝트: {}, 기능: {}",
                    projectId, newFeature.getFeatureId(), e);

            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    @Async("scheduleTaskExecutor")
    public CompletableFuture<Void> rescheduleFromFeatureUpdateAsync(Long projectId, FeatureItem updatedFeature,
                                                                    Integer oldPriorityLevel, Double oldEstimatedTime) {
        try {
            log.info("비동기 기능 수정 재스케줄링 시작 - 프로젝트: {}, 기능: {}",
                    projectId, updatedFeature.getFeatureId());

            long startTime = System.currentTimeMillis();
            scheduleService.rescheduleFromFeatureUpdate(projectId, updatedFeature, oldPriorityLevel, oldEstimatedTime);
            long endTime = System.currentTimeMillis();

            log.info("비동기 기능 수정 재스케줄링 완료 - 프로젝트: {}, 소요시간: {}ms",
                    projectId, (endTime - startTime));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("비동기 기능 수정 재스케줄링 실패 - 프로젝트: {}, 기능: {}",
                    projectId, updatedFeature.getFeatureId(), e);

            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    @Async("scheduleTaskExecutor")
    public CompletableFuture<Void> rescheduleFromFeatureDeleteAsync(Long projectId, FeatureItem deletedFeature) {
        try {
            log.info("비동기 기능 삭제 재스케줄링 시작 - 프로젝트: {}, 기능: {}",
                    projectId, deletedFeature.getFeatureId());

            long startTime = System.currentTimeMillis();
            scheduleService.rescheduleFromFeatureDelete(projectId, deletedFeature);
            long endTime = System.currentTimeMillis();

            log.info("비동기 기능 삭제 재스케줄링 완료 - 프로젝트: {}, 소요시간: {}ms",
                    projectId, (endTime - startTime));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("비동기 기능 삭제 재스케줄링 실패 - 프로젝트: {}, 기능: {}",
                    projectId, deletedFeature.getFeatureId(), e);

            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    @Async("scheduleTaskExecutor")
    public CompletableFuture<Void> rescheduleProjectAsync(Long projectId) {
        try {
            log.info("비동기 프로젝트 전체 재스케줄링 시작 - 프로젝트: {}", projectId);

            long startTime = System.currentTimeMillis();
            scheduleService.rescheduleProject(projectId);
            long endTime = System.currentTimeMillis();

            log.info("비동기 프로젝트 전체 재스케줄링 완료 - 프로젝트: {}, 소요시간: {}ms",
                    projectId, (endTime - startTime));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("비동기 프로젝트 전체 재스케줄링 실패 - 프로젝트: {}", projectId, e);

            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }
}