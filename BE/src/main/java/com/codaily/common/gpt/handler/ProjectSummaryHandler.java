package com.codaily.common.gpt.handler;

import com.codaily.project.dto.ProjectSummaryContent;
import com.codaily.project.dto.ProjectSummaryRequest;
import com.codaily.project.dto.ProjectSummaryResponse;
import com.codaily.project.service.ProjectService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ProjectSummaryHandler implements SseMessageHandler<ProjectSummaryResponse> {

    private final ProjectService projectService;
    private final ObjectMapper objectMapper;

    @Override
    public MessageType getType() {
        return MessageType.PROJECT_SUMMARIZATION;
    }

    @Override
    public Class<ProjectSummaryResponse> getResponseType() {
        return ProjectSummaryResponse.class;
    }

    @Override
    public ProjectSummaryResponse handle(JsonNode content, Long projectId, Long specId, Long featureId) {
        try {
            ProjectSummaryRequest summary = objectMapper.treeToValue(content, ProjectSummaryRequest.class);

            projectService.updateProjectAndSpec(
                    projectId,
                    specId,
                    summary.getProjectTitle(),
                    summary.getProjectDescription(),
                    summary.getSpecTitle()
            );

            return ProjectSummaryResponse.builder()
                    .type("project:summarization")
                    .content(ProjectSummaryContent.builder()
                            .projectTitle(summary.getProjectTitle())
                            .projectDescription(summary.getProjectDescription())
                            .specTitle(summary.getSpecTitle())
                            .projectId(projectId)
                            .specId(specId)
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("프로젝트 요약 저장 실패", e);
            throw new RuntimeException("요약 저장 실패", e);
        }
    }
}
