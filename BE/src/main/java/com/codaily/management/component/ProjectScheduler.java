package com.codaily.management.component;

import com.codaily.management.entity.BatchProcessLog;
import com.codaily.management.repository.BatchProcessLogRepository;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.project.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectScheduler {

    private final ScheduleService scheduleService;
    private final ProjectRepository projectRepository;
    private final BatchProcessLogRepository batchLogRepository;

    @Scheduled(cron = "0 */10 0-5 * * *") // 매일 00:00-05:00, 10분마다
    public void smartDistributedBatchUpdate() {
        LocalDate today = LocalDate.now();

        try {
            // 처리 필요한 프로젝트들 찾기
            Set<Long> projectsNeedingUpdate = findProjectsNeedingWork(today);
            log.info("전체 처리 대상 프로젝트: {}개", projectsNeedingUpdate.size());

            // 이미 처리한 것은 제외
            Set<Long> completedProjects = batchLogRepository.findCompletedProjectIds(today);
            Set<Long> unprocessedProjects = projectsNeedingUpdate.stream()
                    .filter(id -> !completedProjects.contains(id))
                    .collect(Collectors.toSet());

            log.info("남은 처리 대상: {}개 (완료: {}개)", unprocessedProjects.size(), completedProjects.size());

            if (unprocessedProjects.isEmpty()) {
                log.info("✅ 모든 프로젝트 처리 완료!");
                return;
            }

            // 서버 상황에 맞는 배치 크기 결정
            int batchSize = calculateOptimalBatchSize(unprocessedProjects.size());

            // 배치 크기만큼만 처리
            List<Long> currentBatch = unprocessedProjects.stream()
                    .limit(batchSize)
                    .collect(Collectors.toList());

            log.info("이번 배치 처리 대상: {}개 프로젝트", currentBatch.size());

            // 병렬 처리 실행
            processProjectsInParallel(currentBatch, today);

        } catch (Exception e) {
            log.error("스마트 배치 처리 중 오류 발생", e);
        }
    }

    private Set<Long> findProjectsNeedingWork(LocalDate today) {
        Set<Long> projectsNeedingUpdate = new HashSet<>();
        LocalDate yesterday = today.minusDays(1);

        // 지연된 기능이 있는 프로젝트들
        List<Long> projectsWithOverdueFeatures = projectRepository.findProjectsWithOverdueFeatures(yesterday);
        projectsNeedingUpdate.addAll(projectsWithOverdueFeatures);
        log.debug("지연 기능 보유 프로젝트: {}개", projectsWithOverdueFeatures.size());

        // 오늘 시작할 기능이 있는 프로젝트들
        List<Long> projectsWithTodayFeatures = projectRepository.findProjectsWithTodayFeatures(today);
        projectsNeedingUpdate.addAll(projectsWithTodayFeatures);
        log.debug("오늘 시작 기능 보유 프로젝트: {}개", projectsWithTodayFeatures.size());

        // 진행 중인 기능이 있는 프로젝트들 (체크리스트 업데이트 필요)
        List<Long> projectsWithInProgressFeatures = projectRepository.findProjectsWithInProgressFeatures();
        projectsNeedingUpdate.addAll(projectsWithInProgressFeatures);
        log.debug("진행 중 기능 보유 프로젝트: {}개", projectsWithInProgressFeatures.size());

        log.info("전체 활성 프로젝트 중 {}개만 처리 필요", projectsNeedingUpdate.size());
        return projectsNeedingUpdate;
    }


    private int calculateOptimalBatchSize(int remainingCount) {
        // 사용 가능한 프로세서 수 고려
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // 메모리 사용량 체크
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        double memoryUsageRatio = (double) (totalMemory - freeMemory) / totalMemory;

        int baseBatchSize;
        if (memoryUsageRatio > 0.8) {
            baseBatchSize = Math.min(10, availableProcessors); // 메모리 부족 시 작게
        } else if (memoryUsageRatio > 0.6) {
            baseBatchSize = Math.min(20, availableProcessors * 2); // 보통
        } else {
            baseBatchSize = Math.min(50, availableProcessors * 3); // 여유 있으면 크게
        }

        // 남은 작업량도 고려
        int optimalSize = Math.min(baseBatchSize, remainingCount);

        log.debug("배치 크기 계산 - 메모리 사용률: {:.1f}%, 배치 크기: {}",
                memoryUsageRatio * 100, optimalSize);

        return optimalSize;
    }

    @Async("scheduleTaskExecutor")
    public CompletableFuture<Void> processProjectsInParallel(List<Long> projectIds, LocalDate processDate) {
        List<CompletableFuture<Void>> futures = projectIds.stream()
                .map(projectId -> processSingleProject(projectId, processDate))
                .collect(Collectors.toList());

        // 모든 병렬 작업 완료 대기
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return allOf.thenRun(() -> {
            long successCount = futures.stream()
                    .mapToLong(future -> future.isCompletedExceptionally() ? 0 : 1)
                    .sum();

            log.info("배치 처리 완료 - 성공: {}/{}", successCount, projectIds.size());
        });
    }


    @Async("scheduleTaskExecutor")
    public CompletableFuture<Void> processSingleProject(Long projectId, LocalDate processDate) {
        // 처리 로그 생성 또는 조회
        BatchProcessLog processLog = batchLogRepository.findByProjectIdAndProcessDate(projectId, processDate)
                .orElse(BatchProcessLog.builder()
                        .projectId(projectId)
                        .processDate(processDate)
                        .status(BatchProcessLog.ProcessStatus.PENDING)
                        .processingType("DAILY_STATUS")
                        .build());

        // 이미 완료됐으면 스킵
        if (processLog.getStatus() == BatchProcessLog.ProcessStatus.COMPLETED ||
                processLog.getStatus() == BatchProcessLog.ProcessStatus.SKIPPED) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            // 처리 시작 표시
            processLog.markAsProcessing();
            batchLogRepository.save(processLog);

            // 실제 처리 실행
            long startTime = System.currentTimeMillis();

            scheduleService.updateInProgressEstimatedTime(projectId);
            scheduleService.handleOverdueFeatures(projectId);
            scheduleService.startTodayFeatures(projectId, processDate);

            long endTime = System.currentTimeMillis();

            // 완료 표시
            processLog.markAsCompleted();
            batchLogRepository.save(processLog);

            log.debug("프로젝트 {} 처리 완료 - 소요시간: {}ms", projectId, (endTime - startTime));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            // 실패 처리
            processLog.markAsFailed(e.getMessage());
            batchLogRepository.save(processLog);

            log.error("프로젝트 {} 처리 실패", projectId, e);

            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    @Scheduled(cron = "0 */30 * * * *") // 30분마다
    public void retryFailedBatchJobs() {
        LocalDate today = LocalDate.now();

        List<BatchProcessLog> failedJobs = batchLogRepository.findFailedProjectsForRetry(today);

        if (failedJobs.isEmpty()) {
            log.debug("재시도할 실패 작업이 없습니다.");
            return;
        }

        log.info("실패 작업 재시도 시작 - 대상: {}개", failedJobs.size());

        for (BatchProcessLog failedJob : failedJobs) {
            retryProject(failedJob);
        }
    }

    @Async("scheduleTaskExecutor")
    public CompletableFuture<Void> retryProject(BatchProcessLog failedJob) {
        Long projectId = failedJob.getProjectId();

        if (!failedJob.canRetry()) {
            log.warn("프로젝트 {} 재시도 횟수 초과 ({}회), 포기", projectId, failedJob.getRetryCount());
            return CompletableFuture.completedFuture(null);
        }

        try {
            log.info("🔄 프로젝트 {} 재시도 시작 ({}회째)", projectId, failedJob.getRetryCount() + 1);

            // 재시도 시작 표시
            failedJob.markAsProcessing();
            batchLogRepository.save(failedJob);

            // 실제 처리 재실행
            long startTime = System.currentTimeMillis();

            scheduleService.updateInProgressEstimatedTime(projectId);
            scheduleService.handleOverdueFeatures(projectId);
            scheduleService.startTodayFeatures(projectId, failedJob.getProcessDate());

            long endTime = System.currentTimeMillis();

            // 재시도 성공
            failedJob.markAsCompleted();
            batchLogRepository.save(failedJob);

            log.info("프로젝트 {} 재시도 성공 - 소요시간: {}ms", projectId, (endTime - startTime));

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            failedJob.markAsFailed("재시도 실패: " + e.getMessage());
            batchLogRepository.save(failedJob);

            log.error("프로젝트 {} 재시도 실패 ({}회째)", projectId, failedJob.getRetryCount(), e);

            CompletableFuture<Void> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    @Scheduled(cron = "0 0 */1 * * *") // 1시간마다
    public void cleanupStuckProcesses() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);

        List<BatchProcessLog> stuckProcesses = batchLogRepository.findStuckProcesses(threshold);

        if (stuckProcesses.isEmpty()) {
            return;
        }

        log.warn("좀비 프로세스 정리 - 대상: {}개", stuckProcesses.size());

        for (BatchProcessLog stuckProcess : stuckProcesses) {
            stuckProcess.markAsFailed("처리 시간 초과 (1시간+)");
            batchLogRepository.save(stuckProcess);

            log.warn("좀비 프로세스 정리 - 프로젝트: {}, 시작 시간: {}",
                    stuckProcess.getProjectId(), stuckProcess.getStartedAt());
        }
    }


    @Scheduled(cron = "0 30 5 * * *") // 매일 오전 5:30 (배치 처리 마무리 후)
    public void checkMissedProjects() {
        LocalDate today = LocalDate.now();

        List<Long> unprocessedProjects = batchLogRepository.findUnprocessedActiveProjects(today);

        if (unprocessedProjects.isEmpty()) {
            log.info("모든 활성 프로젝트 처리 완료");
            return;
        }

        log.warn("누락된 프로젝트 발견 - {}개, 보완 처리 시작", unprocessedProjects.size());

        for (Long projectId : unprocessedProjects) {
            // 누락된 프로젝트에 대한 로그 생성
            BatchProcessLog missedLog = BatchProcessLog.builder()
                    .projectId(projectId)
                    .processDate(today)
                    .status(BatchProcessLog.ProcessStatus.PENDING)
                    .processingType("MISSED_RECOVERY")
                    .build();

            // 보완 처리 실행
            retryProject(missedLog);
        }
    }
}