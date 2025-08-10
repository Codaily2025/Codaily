package com.codaily.codereview.service;

import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.common.git.WebhookPayload;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeCommitServiceImpl implements CodeCommitService{

    private final CodeCommitRepository codeCommitRepository;
    private final ProjectService projectService;

    @Override
    public CodeCommit findById(Long commitId) {
        return codeCommitRepository.findById(commitId)
                .orElseThrow(() -> new IllegalArgumentException("커밋을 찾을 수 없습니다."));
    }

    @Override
    public Long saveCommitAndReturnId(WebhookPayload.Commit commit, Long projectId) {
        CodeCommit entity = CodeCommit.builder()
                .project(projectService.findById(projectId))
                .commitHash(commit.getId())
                .message(commit.getMessage())
                .committedAt(LocalDateTime.parse(commit.getTimestamp()))
                .build();

        return codeCommitRepository.save(entity).getCommitId();
    }

    @Override
    public List<CodeCommit> findByFeature_FeatureIdOrderByCommittedAtDesc(Long featureId) {
        return codeCommitRepository.findByFeature_FeatureIdOrderByCommittedAtDesc(featureId);
    }
}
