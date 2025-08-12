package com.codaily.retrospective.component;

import com.codaily.project.entity.Project;
import com.codaily.project.repository.ProjectRepository;
import com.codaily.retrospective.service.RetrospectiveGenerateService;
import com.codaily.retrospective.service.RetrospectiveService;
import com.codaily.retrospective.service.RetrospectiveTriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class RetrospectiveScheduler {

    private final ProjectRepository projectRepository;
    private final RetrospectiveService retrospectiveService;
    private final RetrospectiveGenerateService retrospectiveGenerateService;

    /**
     * 매일 밤 00시에 모든 프로젝트에 대해 일일 회고 생성을 비동기적으로 실행합니다.
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00시 정각 실행
    public void generateDailyRetrospectives() {
        log.info("일일 회고 스케줄러 시작");

        List<Project> projects = projectRepository.findAll();
        log.info("총 {}개의 프로젝트에 대해 회고 생성을 시도합니다.", projects.size());

        for (Project project : projects) {
            retrospectiveGenerateService.generateProjectDailyRetrospective(project, RetrospectiveTriggerType.AUTO);
        }

        log.info("일일 회고 스케줄러 완료");
    }
}
