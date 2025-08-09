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


    // Service
    @Transactional
    public void saveRetrospective(Project project,
                                  RetrospectiveGenerateResponse resp,
                                  LocalDate date,
                                  RetrospectiveTriggerType triggerType) {

        String summaryJson = safeSummaryJson(resp); // null 방지
        Retrospective entity = Retrospective.builder()
                .project(project)
                .date(date)                         // ★ 필수
                .content(resp.getContentMarkdown())
                .summaryJson(summaryJson)           // ★ nullable=false이므로 반드시 값
                .triggerType(triggerType)
                .build();

        retrospectiveRepository.save(entity);
    }

    private String safeSummaryJson(RetrospectiveGenerateResponse resp) {
        // 이미 JSON 문자열로 들어온 필드가 있으면 그대로 사용
        // (필요하다면 RetrospectiveGenerateResponse에 summaryJson 필드 추가 가능)
        // 현재 구조상 직접 직렬화하는 게 맞음
        Map<String, Object> root = new LinkedHashMap<>();

        // metrics → 여기선 productivityMetrics를 metrics로 매핑
        if (resp.getProductivityMetrics() != null) {
            root.put("metrics", resp.getProductivityMetrics());
        }

        // summary
        if (resp.getSummary() != null) {
            root.put("summary", resp.getSummary());
        }

        // actionItems
        if (resp.getActionItems() != null && !resp.getActionItems().isEmpty()) {
            root.put("actionItems", resp.getActionItems());
        }

        // completedFeatures
        if (resp.getCompletedFeatures() != null && !resp.getCompletedFeatures().isEmpty()) {
            root.put("completedFeatures", resp.getCompletedFeatures());
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.warn("safeSummaryJson 직렬화 실패", e);
            return "{}";
        }
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
    @Transactional
    public RetrospectiveGenerateResponse getDailyRetrospective(Long projectId, LocalDate date) {
        return toResponse(retrospectiveRepository.findByProject_ProjectIdAndDate(projectId, date));
    }

    @Override
    @Transactional
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
        var root = readTreeSafe(e.getSummaryJson());
        var summaryNode = root != null ? root.path("summary") : null;

        RetrospectiveSummary summary = null;
        if (summaryNode != null && !summaryNode.isMissingNode() && summaryNode.isObject()) {
            try {
                summary = objectMapper.treeToValue(summaryNode, RetrospectiveSummary.class);
            } catch (Exception ex) {
                log.warn("summary node 매핑 실패", ex);
            }
        }

        Project project = e.getProject();
        long userId = e.getProject().getUserId();
        RetrospectiveTriggerType type = e.getTriggerType();

        RetrospectiveGenerateRequest data = collectRetrospectiveData(project, userId, type);

        return RetrospectiveGenerateResponse.builder()
                .contentMarkdown(e.getContent())
                .date(e.getDate())
                .projectId(project.getProjectId())
                .userId(userId)
                .triggerType(e.getTriggerType())
                .productivityMetrics(data.getProductivityMetrics())
                .completedFeatures(data.getCompletedFeatures())
                .summary(summary)
                .build();
    }

    private com.fasterxml.jackson.databind.JsonNode readTreeSafe(String json) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readTree(json); }
        catch (Exception ex) { log.warn("summaryJson 역직렬화 실패", ex); return null; }
    }

}
