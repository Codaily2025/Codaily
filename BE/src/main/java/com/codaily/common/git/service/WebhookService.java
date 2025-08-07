package com.codaily.common.git.service;

import com.codaily.codereview.dto.*;
import com.codaily.common.git.WebhookPayload;

import java.util.List;

public interface WebhookService {
    void handlePushEvent(WebhookPayload payload, Long userId);

    public List<DiffFile> getDiffFilesFromCommit(WebhookPayload.Commit commit, String accessToken);

    public void sendDiffFilesToPython(Long projectId,
                                      Long commitId,
                                      String commitHash,
                                      List<DiffFile> diffFiles);

    public List<FullFile> getFullFilesFromCommit(String commitHash, Long projectId, Long userId);
    public List<FullFile> getFullFilesByPaths(String commitHash, Long projectId, Long userId, List<String> filePaths);

//    void sendChecklistEvaluationRequest(Long projectId, Long featureId, String featureName, List<FullFile> fullFiles, List<ChecklistItemDto> checklistItems);

//    public void sendCodeReviewItemRequest(ChecklistEvaluationResponseDto responseDto);

        //    public List<String> getChecklistByFeatureName(String featureName);

    }
