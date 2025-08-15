package com.codaily.retrospective.service;

import com.codaily.project.entity.Project;
import com.codaily.retrospective.dto.RetrospectiveGenerateRequest;
import com.codaily.retrospective.dto.RetrospectiveGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class RetrospectiveGenerateServiceImpl implements RetrospectiveGenerateService {

    private final WebClient langchainWebClient;
    private final RetrospectiveService retrospectiveService;

    @Override
    public CompletableFuture<RetrospectiveGenerateResponse> generateProjectDailyRetrospective(Project project, RetrospectiveTriggerType type) {
        LocalDate today = type == RetrospectiveTriggerType.AUTO ? LocalDate.now(ZoneId.of("Asia/Seoul")) : LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

        return Mono.fromCallable(() -> {
                    // 1) 이미 오늘 회고가 있으면 → DTO만 반환(저장 금지)
                    if (retrospectiveService.existsByProjectAndDate(project, today)) {
                        log.info("Retrospective already exists. projectId={}, date={}", project.getProjectId(), today);
                        return (Object) retrospectiveService.getDailyRetrospective(project.getProjectId(), today);
                    }

                    // 2) 신규 생성 필요 → 재료 수집
                    final Long userId = project.getUserId();
                    RetrospectiveGenerateRequest payload =
                            retrospectiveService.collectRetrospectiveData(project, userId, type);

                    // 3) 재료가 전무하면 생성 스킵
                    boolean noCompleted = payload.getCompletedFeatures() == null || payload.getCompletedFeatures().isEmpty();
                    boolean noMetrics = payload.getProductivityMetrics() == null
                            || (payload.getProductivityMetrics().getCompletedFeatures() == 0
                            && payload.getProductivityMetrics().getTotalCommits() == 0);

                    if (noCompleted && noMetrics) {
                        log.info("No retrospective materials. Skip generation. projectId={}, date={}", project.getProjectId(), today);
                        return null; // 아래 flatMap에서 Mono.empty() 처리
                    }

                    // 4) 이어지는 단계에서 WebClient 호출
                    return payload;
                })
                .subscribeOn(Schedulers.boundedElastic()) // 블로킹(IO/DB) 분리
                .flatMap(obj -> {
                    if (obj == null) {
                        return Mono.empty(); // 재료 없음 → 아무 것도 생성하지 않음
                    }
                    if (obj instanceof RetrospectiveGenerateResponse ready) {
                        // 이미 존재했던 케이스 → 저장 금지, 그대로 반환
                        return Mono.just(ready);
                    }

                    // 신규 생성: LLM 서버 호출
                    RetrospectiveGenerateRequest payload = (RetrospectiveGenerateRequest) obj;
                    return langchainWebClient.post()
                            .uri("/ai/api/retrospective/generate")
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(RetrospectiveGenerateResponse.class)
                            .publishOn(Schedulers.boundedElastic())
                            .flatMap(resp -> Mono.fromRunnable(() ->
                                            retrospectiveService.saveRetrospective(
                                                    project, resp, today, type))
                                    .thenReturn(resp));
                })
                .toFuture();
    }
}
