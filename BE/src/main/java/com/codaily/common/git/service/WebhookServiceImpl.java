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
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    @Qualifier("githubWebClient")
    private final WebClient githubWebClient;
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
            log.info("커밋: {}", commit.getId());
            log.info("메시지: {}", commit.getMessage());
            log.info("추가된 파일: {}", commit.getAdded());
            log.info("수정된 파일: {}", commit.getModified());
            log.info("삭제된 파일: {}", commit.getRemoved());

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
            String fullName = payload.getRepository().getFull_name();
            String[] parts = fullName.split("/");
            String repoOwner = parts[0];
            String repoName = parts[1];
            String commitBranch = payload.getRef().replace("refs/heads/", "");

            CommitInfoDto commitInfoDto = CommitInfoDto.builder().repoName(repoName).repoOwner(repoOwner).build();

            Long projectId = repositories.getProject().getProjectId();

            sendDiffFilesToPython(projectId, commitId, commit.getId(), commit.getMessage(), diffFiles, userId, commitInfoDto, commitBranch);
        }
    }

    @Override
    public List<DiffFile> getDiffFilesFromCommit(WebhookPayload.Commit commit, String accessToken) {
        String commitUrl = commit.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(commitUrl, HttpMethod.GET, entity, JsonNode.class);

        List<DiffFile> diffFiles = new ArrayList<>();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode filesNode = response.getBody().get("files");

            // 제외할 경로/패턴 목록
            List<String> excludedPatterns = List.of(
                    ".idea/", ".vscode/", "node_modules/", "build/", "target/",
                    ".gradle/", ".git/", ".github/", ".DS_Store"
            );

            for (JsonNode file : filesNode) {
                String filename = file.get("filename").asText();

                // 제외 패턴 필터링
                if (excludedPatterns.stream().anyMatch(filename::startsWith)) {
                    continue;
                }

                String patch = file.has("patch") ? file.get("patch").asText() : "";
                String status = file.has("status") ? file.get("status").asText() : "modified";
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
                                      Long userId,
                                      CommitInfoDto commitInfoDto,
                                      String commitBranch) {

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8000") // Python 서버 전용
                .build();

        List<FeatureItem> featureItems = featureItemRepository.findByProject_ProjectId(projectId);
        List<String> availableFeatures = featureItems.stream()
                .filter(featureItem -> featureItem.getParentFeature() != null)
                .map(FeatureItem::getTitle)
                .toList();

        FeatureInferenceRequestDto requestDto = FeatureInferenceRequestDto.builder()
                .projectId(projectId)
                .commitId(commitId)
                .commitHash(commitHash)
                .commitMessage(commitMessage)
                .diffFiles(diffFiles)
                .availableFeatures(availableFeatures)
                .jwtToken(userService.getGithubAccessToken(userId))
                .commitInfoDto(commitInfoDto)
                .forceDone(false)
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
    public List<FullFile> getFullFilesFromCommit(String commitHash, Long projectId, Long userId, String repoOwner, String repoName) {
        String token = userService.getGithubAccessToken(userId);

        String commitUrl = String.format("%s/repos/%s/%s/commits/%s", githubApiUrl, repoOwner, repoName, commitHash);
        Mono<Map<String, Object>> responseMono = githubWebClient.get()
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
            String contentUrl = String.format("%s/repos/%s/%s/contents/%s?ref=%s", githubApiUrl, repoOwner, repoName, filePath, commitHash);

            String content = githubWebClient.get()
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
    public List<FullFile> getFullFilesByPaths(String commitHash, Long projectId, Long userId, List<String> filePaths, String repoOwner, String repoName) {
        List<FullFile> allFiles = getFullFilesFromCommit(commitHash, projectId, userId, repoOwner, repoName);

        return allFiles.stream()
                .filter(file -> filePaths.contains(file.getFilePath()))
                .collect(Collectors.toList());
    }



    //////////////////////////////////////////////////
    // Test

    @Override
    public List<DiffFile> getDiffFilesFromCommitTest(String commitUrl, String accessToken) {
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
    public void sendDiffFilesToPythonTest(Long projectId,
                                      Long commitId,
                                      String commitHash,
                                      String commitMessage,
                                      List<DiffFile> diffFiles,
                                      Long userId,
                                      CommitInfoDto commitInfoDto) {

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8000") // Python 서버 전용
                .build();

        List<FeatureItem> featureItems = featureItemRepository.findByProject_ProjectId(projectId);
        List<String> availableFeatures = featureItems.stream()
                .map(FeatureItem::getTitle)
                .toList();


//        FeatureInferenceRequestDto requestDto = FeatureInferenceRequestDto.builder()
//                .projectId(projectId)
//                .commitId(commitId)
//                .commitHash(commitHash)
//                .commitMessage(commitMessage)
//                .diffFiles(diffFiles)
//                .availableFeatures(availableFeatures)
//                .jwtToken(userService.getGithubAccessToken(userId))
//                .commitInfoDto(commitInfoDto)
//                .build();
        FeatureInferenceRequestDto requestDto = FeatureInferenceRequestDto.builder()
                .projectId(30L)
                .commitId(6L)
                .commitHash("1b49de6aed12f7ea2fb354333f442dcd64643144")
                .commitMessage("feat: user 클래스 만드는 중")
                .diffFiles(getDiffFilesFromCommitTest("https://api.github.com/repos/codailyTest/codailyTest/commits/1b49de6aed12f7ea2fb354333f442dcd64643144", "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"))
                .availableFeatures(availableFeatures)
                .jwtToken("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")
                .commitInfoDto(CommitInfoDto.builder().repoName("codailyTest").repoOwner("codailyTest").build())
                .build();

        log.info("📤 Sending to Python: {}", requestDto);

        webClient.post()
                .uri("/api/code-review/feature-inference")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
//                .toBodilessEntity()
                .doOnSuccess(res -> log.info("✅ Python 서버로 diffFiles 전송 성공"))
                .doOnError(error -> log.error("❌ 전송 실패", error))
                .subscribe(); // ✅ 비동기 실행 (subscribe 없으면 실행 안됨)
    }

//    private String getFileFromRepoPath(String token, String owner, String repo, String path, @Nullable String ref) {
//        String encodedPath = URLEncoder.encode(path, StandardCharsets.UTF_8);
//        String base = String.format("%s/repos/%s/%s/contents/%s", githubApiUrl, owner, repo, encodedPath);
//        String url = (ref == null || ref.isBlank()) ? base : base + "?ref=" + URLEncoder.encode(ref, StandardCharsets.UTF_8);
//
//        return webClient.get()
//                .uri(url)
//                .headers(h -> {
//                    h.setBearerAuth(token);
//                    // RAW 로 받아서 base64 디코딩 없이 본문을 그대로 문자열로 수신
//                    h.set(HttpHeaders.ACCEPT, "application/vnd.github.v3.raw");
//                })
//                .retrieve()
//                // 404면 빈 문자열로 처리(경로 없음)
//                .bodyToMono(String.class)
//                .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.just(""))
//                .block();
//    }

    private String getFileFromRepoPath(String token, String owner, String repo, String path, @Nullable String ref) {
        // 1) 경로 정규화: 백슬래시 → 슬래시, 앞쪽 슬래시 제거
        String normalized = path.replace('\\', '/').replaceAll("^/+", "");

        return githubWebClient.get()
                .uri(uriBuilder -> {
                    // githubApiUrl 이 "https://api.github.com" 라고 가정
                    // base 는 WebClient 생성 시 baseUrl 로 놔도 되고, 여기선 pathSegment만 안전하게 쌓음
                    var ub = uriBuilder
                            .pathSegment("repos", owner, repo, "contents");

                    // 2) 디렉터리/파일 세그먼트를 분리해서 각각 pathSegment 로 추가(슬래시는 구분자로만 사용)
                    for (String seg : normalized.split("/")) {
                        if (!seg.isBlank()) ub = ub.pathSegment(seg);
                    }

                    // 3) ref 가 있으면 쿼리로 추가(브랜치/태그/커밋 SHA)
                    if (ref != null && !ref.isBlank()) {
                        ub = ub.queryParam("ref", ref);
                    }
                    return ub.build();
                })
                .headers(h -> {
                    h.setBearerAuth(token);
                    // RAW 로 받아 base64 디코딩 불필요
                    h.set(HttpHeaders.ACCEPT, "application/vnd.github.v3.raw");
                })
                .retrieve()
                // 404(경로 없음) → 빈 문자열로
                .bodyToMono(String.class)
                .onErrorReturn("")
                .block();
    }

    /** 레포지토리의 특정 ref(없으면 기본 브랜치) 기준으로, 주어진 경로 리스트의 파일 본문 전부 가져오기 */
    public List<FullFile> getFilesFromRepoPaths(Long userId, String owner, String repo, List<String> filePaths, @Nullable String ref) {
        String token = userService.getGithubAccessToken(userId);

        List<FullFile> result = new ArrayList<>();
        for (String path : filePaths) {
            String content = getFileFromRepoPath(token, owner, repo, path, ref);
            result.add(FullFile.builder()
                    .filePath(path)
                    .content(content) // 없거나 바이너리면 빈 문자열일 수 있음
                    .build());
        }
        return result;
    }
}

