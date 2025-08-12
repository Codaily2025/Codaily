package com.codaily.common.git.controller;

import com.codaily.codereview.dto.CommitInfoDto;
import com.codaily.codereview.dto.DiffFile;
import com.codaily.common.git.WebhookPayload;
import com.codaily.common.git.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Slf4j
public class GithubTestController {

    private final WebhookService webhookService;

    private List<DiffFile> lastDiffFiles = new ArrayList<>();

    @GetMapping("/diff-files")
    public ResponseEntity<List<DiffFile>> getDiffFiles() {
        String commitUrl = "https://api.github.com/repos/codailyTest/codailyTest/commits/1b49de6aed12f7ea2fb354333f442dcd64643144\n";
        String accessToken = "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";

        lastDiffFiles = webhookService.getDiffFilesFromCommitTest(commitUrl, accessToken);
        return ResponseEntity.ok(lastDiffFiles);
    }

    @PostMapping("/send-diff-files")
    public void sendDiffFilesToPython() {

        webhookService.sendDiffFilesToPythonTest(
                30L,
                6L,
                "1b49de6aed12f7ea2fb354333f442dcd64643144",
                "feat: user 클래스 만드는 중",
                lastDiffFiles,
                6L,
                CommitInfoDto.builder()
                        .repoOwner("codailyTest")
                        .repoName("codailyTest")
                        .build()
                );
    }


}