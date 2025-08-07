package com.codaily.common.git.service;

import com.codaily.auth.service.UserService;
import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.ChangeType;
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.common.git.WebhookPayload;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.ProjectRepositories;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepositoriesRepository;
import com.codaily.project.service.ProjectRepositoriesService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {

    private final FeatureItemRepository featureItemRepository;
    private final UserService userService;
    private final ProjectRepositoriesRepository projectRepositoriesRepository;
    private final WebClient webClient;
    private final CodeCommitRepository codeCommitRepository;
    private final ProjectRepositoriesService projectRepositoriesService;



    @Value("${github.api-url}")
    private String githubApiUrl;

    @Override
    public void handlePushEvent(WebhookPayload payload, Long userId) {
        List<WebhookPayload.Commit> commits = payload.getCommits();
        String repo = payload.getRepository().getFull_name();
        String accessToken = userService.getGithubAccessToken(userId);

        for (WebhookPayload.Commit commit : commits) {
            log.info("🧾 커밋: {}", commit.getId());
            log.info("📄 메시지: {}", commit.getMessage());
            log.info("➕ 추가된 파일: {}", commit.getAdded());
            log.info("📝 수정된 파일: {}", commit.getModified());
            log.info("➖ 삭제된 파일: {}", commit.getRemoved());

            List<DiffFile> diffFiles = getDiffFilesFromCommit(commit,accessToken);

            if(diffFiles.isEmpty()) {
                log.info("변경된 파일이 없습니다. 코드리뷰를 생략합니다.");
                continue;
            }
            ProjectRepositories repositories = projectRepositoriesService.getRepoByName(repo);
            CodeCommit entity = CodeCommit.builder()
                            .commitHash(commit.getId())
                            .author(commit.getAuthor().getName())
                            .project(repositories.getProject())
                            .message(commit.getMessage())
                            .committedAt(LocalDateTime.parse(commit.getTimestamp())).build();

            Long commitId = codeCommitRepository.save(entity).getCommitId();
            Long projectId = repositories.getProject().getProjectId();

            sendDiffFilesToPython(projectId, commitId, commit.getId(), commit.getMessage(), diffFiles, userId);
        }
    }

    @Override
    public List<DiffFile> getDiffFilesFromCommit(WebhookPayload.Commit commit, String accessToken) {
        String commitUrl = commit.getUrl(); // payload에 포함된 URL
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(commitUrl, HttpMethod.GET, entity, JsonNode.class);

        List<DiffFile> diffFiles = new ArrayList<>();

        if (response.getStatusCode().is2xxSuccessful()) {
            JsonNode filesNode = response.getBody().get("files");
            for (JsonNode file : filesNode) {
                String filename = file.get("filename").asText();
                String patch = file.has("patch") ? file.get("patch").asText() : "";
                String status = file.has("status") ? file.get("status").asText() : "modified"; // "added", "removed", "modified"
                ChangeType changeType = ChangeType.fromString(status);
                diffFiles.add(new DiffFile(filename, patch, changeType));
            }
        }

        return diffFiles;
    }


    @Override
    @Async
    public void sendDiffFilesToPython(Long projectId,
                                      Long commitId,
                                      String commitHash,
                                      String commitMessage,
                                      List<DiffFile> diffFiles,
                                      Long userId) {

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8000") // Python 서버 전용
                .build();

        List<FeatureItem> featureItems = featureItemRepository.findByProject_ProjectId(projectId);
        List<String> availableFeatures = featureItems.stream()
                .map(FeatureItem::getTitle)
                .toList();

        FeatureInferenceRequestDto requestDto = FeatureInferenceRequestDto.builder()
                .projectId(projectId)
                .commitId(commitId)
                .commitHash(commitHash)
                .commitMessage(commitMessage)
                .diffFiles(diffFiles)
                .availableFeatures(availableFeatures)
                .jwtToken(userService.getGithubAccessToken(userId)) // ✅ 기존 jwtToken 그대로 전달
                .build();

        webClient.post()
                .uri("/api/code-review/feature-inference")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.info("✅ Python 서버로 diffFiles 전송 성공"))
                .doOnError(error -> log.error("❌ 전송 실패", error))
                .subscribe(); // ✅ 비동기 실행 (subscribe 없으면 실행 안됨)
    }

    @Override
    public List<FullFile> getFullFilesFromCommit(String commitHash, Long projectId, Long userId) {
        String token = userService.getGithubAccessToken(userId);
        String owner = userService.findById(userId).getGithubAccount();

        ProjectRepositories repoInfo = projectRepositoriesRepository.findByProject_Id(projectId)
                .orElseThrow(() -> new IllegalArgumentException("연결된 GitHub repo 없음"));
        String repo = repoInfo.getRepoName();

        String commitUrl = String.format("%s/repos/%s/%s/commits/%s", githubApiUrl, owner, repo, commitHash);
        Mono<Map<String, Object>> responseMono = webClient.get()
                .uri(commitUrl)
                .headers(h -> {
                    h.setBearerAuth(token);
                    h.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});

        Map<String, Object> commitResponse = responseMono.block();

        List<Map<String, Object>> files = (List<Map<String, Object>>) commitResponse.get("files");
        if (files == null) return Collections.emptyList();

        List<FullFile> fullFiles = new ArrayList<>();
        for (Map<String, Object> file : files) {
            String filePath = (String) file.get("filename");
            String contentUrl = String.format("%s/repos/%s/%s/contents/%s?ref=%s", githubApiUrl, owner, repo, filePath, commitHash);

            String content = webClient.get()
                    .uri(contentUrl)
                    .headers(h -> {
                        h.setBearerAuth(token);
                        h.set(HttpHeaders.ACCEPT, "application/vnd.github.v3.raw");
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorReturn("")
                    .block();

            fullFiles.add(FullFile.builder()
                    .filePath(filePath)
                    .content(content)
                    .build());
        }

        return fullFiles;
    }

    @Override
    public List<FullFile> getFullFilesByPaths(String commitHash, Long projectId, Long userId, List<String> filePaths) {
        List<FullFile> allFiles = getFullFilesFromCommit(commitHash, projectId, userId);

        return allFiles.stream()
                .filter(file -> filePaths.contains(file.getFilePath()))
                .collect(Collectors.toList());
    }
}
