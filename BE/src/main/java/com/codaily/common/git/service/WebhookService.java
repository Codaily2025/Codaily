package com.codaily.common.git.service;

import com.codaily.codereview.dto.*;
import com.codaily.common.git.WebhookPayload;

import java.util.List;

public interface WebhookService {
    void handlePushEvent(WebhookPayload payload, Long userId);

    List<DiffFile> getDiffFilesFromCommit(WebhookPayload.Commit commit, String accessToken);

    void sendDiffFilesToPython(Long projectId,
                               Long commitId,
                               String commitHash,
                               String commitMessage,
                               List<DiffFile> diffFiles,
                               Long userId,
                               CommitInfoDto commitInfoDto);

    List<FullFile> getFullFilesFromCommit(String commitHash, Long projectId, Long userId, String repoOwner, String repoName);

    List<FullFile> getFullFilesByPaths(String commitHash, Long projectId, Long userId, List<String> filePaths, String repoOwner, String repoName);

    void removeAllHooksForUser(Long userId);

}
