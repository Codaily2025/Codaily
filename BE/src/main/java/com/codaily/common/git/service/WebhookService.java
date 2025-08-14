package com.codaily.common.git.service;

import com.codaily.codereview.dto.*;
import com.codaily.common.git.WebhookPayload;
import jakarta.annotation.Nullable;

import java.util.List;

public interface WebhookService {
    void handlePushEvent(WebhookPayload payload, Long userId);

    public List<DiffFile> getDiffFilesFromCommit(WebhookPayload.Commit commit, String accessToken);

    public void sendDiffFilesToPython(Long projectId,
                                      Long commitId,
                                      String commitHash,
                                      String commitMessage,
                                      List<DiffFile> diffFiles,
                                      Long userId,
                                      CommitInfoDto commitInfoDto,
                                      String commitBranch);

    public void sendManualCompleteToPython(Long projectId, Long userId,
                                           Long featureId);

    public List<FullFile> getFullFilesFromCommit(String commitHash, Long projectId, Long userId, String repoOwner, String repoName);
    public List<FullFile> getFullFilesByPaths(String commitHash, Long projectId, Long userId, List<String> filePaths, String repoOwner, String repoName);


    public List<DiffFile> getDiffFilesFromCommitTest(String commitUrl, String accessToken);

    public void sendDiffFilesToPythonTest(Long projectId,
                                      Long commitId,
                                      String commitHash,
                                      String commitMessage,
                                      List<DiffFile> diffFiles,
                                      Long userId,
                                      CommitInfoDto commitInfoDto);
    public List<FullFile> getFilesFromRepoPaths(Long userId, String owner, String repo, List<String> filePaths, @Nullable String ref);
}
