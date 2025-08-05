package com.codaily.common.git.service;

import com.codaily.codereview.dto.ChecklistEvaluationResponseDto;
import com.codaily.codereview.dto.ChecklistItemDto;
import com.codaily.codereview.dto.DiffFile;
import com.codaily.codereview.dto.FullFile;
import com.codaily.common.git.WebhookPayload;

import java.util.List;

public interface WebhookService {
    void handlePushEvent(WebhookPayload payload);

    public List<DiffFile> getDiffFilesFromCommit(WebhookPayload.Commit commit, String accessToken);

    public void sendDiffFilesToPython(Long projectId,
                                      Long commitId,
                                      List<DiffFile> diffFiles);

    public List<FullFile> getFullFilesFromCommit(String commitHash, Long projectId, Long userId);

    void sendChecklistEvaluationRequest(Long projectId, Long featureId, String featureName, List<FullFile> fullFiles, List<ChecklistItemDto> checklistItems);

    public void sendCodeReviewItemRequest(ChecklistEvaluationResponseDto responseDto);

        //    public List<String> getChecklistByFeatureName(String featureName);

    }
