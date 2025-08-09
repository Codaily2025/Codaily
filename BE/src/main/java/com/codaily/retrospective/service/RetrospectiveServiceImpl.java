package com.codaily.retrospective.service;

import com.codaily.codereview.dto.CodeReviewSummaryResponseDto;
import com.codaily.codereview.entity.CodeReviewItem;
import com.codaily.codereview.entity.FeatureItemChecklist;
import com.codaily.codereview.service.CodeReviewItemService;
import com.codaily.codereview.service.CodeReviewService;
import com.codaily.codereview.service.FeatureItemChecklistService;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.repository.DailyProductivityRepository;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.retrospective.dto.*;
import com.codaily.retrospective.entity.Retrospective;
import com.codaily.retrospective.repository.RetrospectiveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class RetrospectiveServiceImpl implements RetrospectiveService {

    private final RetrospectiveRepository retrospectiveRepository;

    private final FeatureItemRepository featureItemRepository;

    private final FeatureItemChecklistService featureItemChecklistService;

    private final CodeReviewService codeReviewService;

    private final CodeReviewItemService codeReviewItemService;

    private final DailyProductivityRepository dailyProductivityRepository;

    private final ObjectMapper objectMapper;


    public void saveRetrospective(Project project, RetrospectiveGenerateResponse response) {
        Retrospective entity = new Retrospective();
        entity.setProject(project);
        entity.setDate(response.getDate());
        entity.setTriggerType(response.getTriggerType());
        entity.setContent(response.getContentMarkdown());

        // summary_json 직렬화
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("summary", response.getSummary());
        json.put("actionItems", response.getActionItems());
        json.put("metrics", response.getProductivityMetrics());
        json.put("completedFeatures", response.getCompletedFeatures());

        try {
            entity.setSummaryJson(objectMapper.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            // 실패 시 최소한 content만 저장하고 로그
            log.error("Failed to serialize summary_json", e);
            entity.setSummaryJson("{}");
        }

        retrospectiveRepository.save(entity);
    }

    @Override
    @Transactional
    public boolean existsByProjectAndDate(Project project, LocalDate date) {
        return retrospectiveRepository.existsByProjectAndDate(project, date);
    }

    @Override
    @Transactional
    public RetrospectiveGenerateRequest collectRetrospectiveData(Project project, Long userId, RetrospectiveTriggerType triggerType) {
        Long projectId = project.getProjectId();
        LocalDate today = LocalDate.now();

        // 1) 오늘 완료된 상세 기능 조회
        List<FeatureItem> completedFeatures = featureItemRepository.findCompletedFeaturesByProjectAndDate(projectId, today);

        // 2) 기능 요약 리스트 생성 (체크리스트/리뷰 요약/품질점수 포함)
        List<RetrospectiveFeatureSummary> featureSummaries = completedFeatures.stream().map(feature -> {
            Long fid = feature.getFeatureId();

            // 체크리스트 달성도
            var checklists = featureItemChecklistService.findByFeatureItem_FeatureId(fid);
            int checklistCount = checklists.size();
            int checklistDoneCount = (int) checklists.stream().filter(FeatureItemChecklist::isDone).count();

            // 코드 리뷰 요약
            double qualityScore = 0.0;
            String summary = "";

            try {
                CodeReviewSummaryResponseDto review = codeReviewService.getCodeReviewSummary(fid);
                qualityScore = review.getQualityScore();
                summary = review.getSummary();
            } catch (IllegalArgumentException e) {
                // 코드 리뷰가 없는 경우 — 정상적인 시나리오일 수 있으므로 WARN
                log.warn("Code review not found for featureId: {}", fid);
                qualityScore = 0.0;
                summary = "";
            } catch (Exception e) {
                // 알 수 없는 예외 — 반드시 원인 로그 남기기
                log.error("Unexpected error while fetching code review for featureId: {}", fid, e);
                qualityScore = 0.0;
                summary = "";
            }

            // 3) 리뷰 이슈
            List<CodeReviewItem> items = codeReviewItemService.getCodeReviewItemById(fid);
            List<RetrospectiveIssueSummary> issueSummaries = items.stream().map(item -> {
                RetrospectiveIssueSummary issue = new RetrospectiveIssueSummary();
                issue.setFeatureTitle(feature.getTitle());
                issue.setChecklistItem(item.getFeatureItemChecklist() != null ? item.getFeatureItemChecklist().getItem() : null);
                issue.setCategory(item.getCategory());
                issue.setSeverity(item.getSeverity());
                issue.setMessage(item.getMessage());
                issue.setFilePath(item.getFilePath());
                return issue;
            }).toList();

            RetrospectiveFeatureSummary result = new RetrospectiveFeatureSummary();
            result.setFeatureId(fid);
            result.setTitle(feature.getTitle());
            result.setField(feature.getField());
            result.setChecklistCount(checklistCount);
            result.setChecklistDoneCount(checklistDoneCount);
            result.setCodeQualityScore(qualityScore);
            result.setSummary(summary);
            result.setReviewIssues(issueSummaries);

            return result;
        }).toList();

        // 4) 생산성 메트릭 (DailyProductivity 있으면 사용, 없으면 0 안전값)
        RetrospectiveProductivityMetrics metrics = new RetrospectiveProductivityMetrics();
        dailyProductivityRepository.findByUserIdAndProjectIdAndDate(userId, projectId, today).ifPresentOrElse(daily -> {
            double avgQuality = featureSummaries.isEmpty() ? 0.0 : featureSummaries.stream().mapToDouble(RetrospectiveFeatureSummary::getCodeQualityScore).average().orElse(0.0);
            metrics.setCodeQuality(avgQuality);
            metrics.setProductivityScore(daily.getProductivityScore());
            metrics.setCompletedFeatures(daily.getCompletedFeaturesOrZero());
            metrics.setTotalCommits(daily.getTotalCommits() != null ? daily.getTotalCommits() : 0);
        }, () -> {
            metrics.setCodeQuality(0.0);
            metrics.setProductivityScore(0.0);
            metrics.setCompletedFeatures(0);
            metrics.setTotalCommits(0);
        });

        // 5) 요청 DTO 조립
        RetrospectiveGenerateRequest req = new RetrospectiveGenerateRequest();
        req.setDate(today);
        req.setProjectId(projectId);
        req.setUserId(userId);
        req.setTriggerType(triggerType);
        req.setCompletedFeatures(featureSummaries);
        req.setProductivityMetrics(metrics);

        return req;
    }

    @Override
    public RetrospectiveGenerateResponse getDailyRetrospective(Long projectId, LocalDate date) {
        return toResponse(retrospectiveRepository.findByProject_ProjectIdAndDate(projectId, date));
    }

    @Override
    public RetrospectiveListResponse getAllDailyRetrospectives(Long projectId) {
        return RetrospectiveListResponse.builder()
                .retrospectives(
                        retrospectiveRepository.findAllByProject_ProjectIdOrderByDateDesc(projectId)
                                .stream()
                                .map(this::toResponse)
                                .toList()
                )
                .build();
    }


    public RetrospectiveGenerateResponse toResponse(Retrospective e){
        return RetrospectiveGenerateResponse.builder()
                .contentMarkdown(e.getContent())                   // 엔티티 content → DTO contentMarkdown
                .summary(readJsonSafe(e.getSummaryJson(), RetrospectiveSummary.class)) // json 문자열 → Map/obj
                .actionItems(null) // 엔티티에 없으면 null/빈 값
                .build();
    }

    private <T> T readJsonSafe(String json, Class<T> type) {
        try { return json == null ? null : objectMapper.readValue(json, type); }
        catch (Exception ex) { log.warn("summaryJson 역직렬화 실패", ex); return null; }
    }

}
