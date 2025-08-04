package com.codaily.management.component;

import com.codaily.project.service.FeatureItemServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectScheduler {
    private final FeatureItemServiceImpl featureItemService;

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyProjectStatusUpdate() {
        featureItemService.updateDailyStatus();
    }
}
