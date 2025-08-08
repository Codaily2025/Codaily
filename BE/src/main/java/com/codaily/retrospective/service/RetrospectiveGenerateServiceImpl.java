package com.codaily.retrospective.service;

import com.codaily.project.entity.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;

@Log4j2
@Service
@RequiredArgsConstructor
public class RetrospectiveGenerateServiceImpl implements RetrospectiveGenerateService {

    private final WebClient langchainWebClient;
    private final RetrospectiveService retrospectiveService;

    @Async
    @Override
    public void generateProjectDailyRetrospective(Project project) {
        if (retrospectiveService.existsByProjectAndDate(project, LocalDate.now())) {
            log.info("이미 오늘 회고가 존재합니다. 생성 생략 - projectId: {}", project.getProjectId());
            return;
        }

        // 2. 회고 재료 수집 (예: 오늘 완료된 Task, 커밋 등)
//        List<Task> todayTasks = taskRepository.findCompletedTasksByProjectAndDate(project.getProjectId(), LocalDate.now());
//        if (todayTasks.isEmpty()) {
//            log.info("오늘 완료된 작업이 없습니다. 회고 생성을 생략합니다 - projectId: {}", project.getProjectId());
//            return;
//        }

        // 3. GPT 서버 요청 payload 구성
        Object dailyProjectInfo = new Object();
//                RetrospectiveRequestDto.builder()
//                .projectId(project.getProjectId())
//                .projectTitle(project.getTitle())
//                .tasks(todayTasks)
//                .build();

        // 4. WebClient로 GPT 서버에 비동기 요청
        langchainWebClient.post()
                .uri("/gpt/retrospective")
                .bodyValue(dailyProjectInfo)
                .retrieve()
                .bodyToMono(String.class)
                .publishOn(Schedulers.boundedElastic()) // JPA 트랜잭션용 스레드로 이동
                .subscribe(response -> {
                    // 5. 응답 기반 회고 저장
                    retrospectiveService.saveRetrospective(project, response);
                }, error -> {
                    log.error("GPT 회고 생성 실패 - projectId: {}", project.getProjectId(), error);
                });
    }
}
