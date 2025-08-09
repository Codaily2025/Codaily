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
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class RetrospectiveGenerateServiceImpl implements RetrospectiveGenerateService {

    private final WebClient langchainWebClient;
    private final RetrospectiveService retrospectiveService;

    @Override
    public CompletableFuture<RetrospectiveGenerateResponse> generateProjectDailyRetrospective(Project project) {
        var today = LocalDate.now();

        return Mono.fromCallable(() -> {
                    // --- 블로킹 구간을 별도 풀로 이동 ---
                    if (retrospectiveService.existsByProjectAndDate(project, today)) {
                        // 이미 존재 → 기존 DTO 반환 (null 허용 시 Optional)
                        return retrospectiveService.getDailyRetrospective(project.getProjectId(), today);
                    }

                    Long userId = project.getUserId();
                    RetrospectiveGenerateRequest payload =
                            retrospectiveService.collectRetrospectiveData(project, userId, RetrospectiveTriggerType.AUTO);

                    // 재료 없으면 null로 조기 종료
                    if ((payload.getCompletedFeatures() == null || payload.getCompletedFeatures().isEmpty())
                            && (payload.getProductivityMetrics() == null
                            || (payload.getProductivityMetrics().getCompletedFeatures() == 0
                            && payload.getProductivityMetrics().getTotalCommits() == 0))) {
                        return CompletableFuture.completedFuture(null);
                    }

                    // 신호용: 이어서 WebClient를 타야 함 → payload를 래핑해서 넘김
                    return payload;
                })
                .subscribeOn(Schedulers.boundedElastic()) // ← 블로킹을 요청/스케줄러 스레드에서 떼어냄
                .flatMap(obj -> {
                    if (obj == null) {
                        return Mono.empty(); // 재료 없음 → 빈 완료
                    }

                    if (obj instanceof RetrospectiveGenerateResponse ready) {
                        return Mono.just(ready); // 이미 존재 → 그 DTO 그대로 반환
                    }
                    // 새로 생성해야 함 → WebClient 호출
                    RetrospectiveGenerateRequest payload = (RetrospectiveGenerateRequest) obj;
                    return langchainWebClient.post()
                            .uri("/gpt/retrospective")
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(RetrospectiveGenerateResponse.class);
                })
                .publishOn(Schedulers.boundedElastic()) // 저장은 블로킹이므로 여기서
                .flatMap(resp -> Mono.fromRunnable(() -> retrospectiveService.saveRetrospective(project, resp))
                        .thenReturn(resp))
                .toFuture();
    }

}
