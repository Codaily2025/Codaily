package com.codaily.codereview.service;

import com.codaily.codereview.entity.CodeCommit;
import com.codaily.common.git.WebhookPayload;

import java.util.List;

public interface CodeCommitService {
    CodeCommit findById(Long commitId);
    Long saveCommitAndReturnId(WebhookPayload.Commit commit, Long projectId);
    List<CodeCommit> findByFeature_FeatureIdOrderByCommittedAtDesc(Long featureId);
}
