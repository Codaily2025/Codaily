package com.codaily.codereview.service;

import com.codaily.codereview.entity.CodeCommit;
import com.codaily.common.git.WebhookPayload;

public interface CodeCommitService {
    CodeCommit findById(Long commitId);
    Long saveCommitAndReturnId(WebhookPayload.Commit commit, Long projectId);

}
