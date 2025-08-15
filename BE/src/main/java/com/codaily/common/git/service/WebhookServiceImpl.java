package com.codaily.common.git.service;

import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.codereview.dto.CommitInfoDto;
import com.codaily.codereview.dto.DiffFile;
import com.codaily.codereview.dto.FeatureInferenceRequestDto;
import com.codaily.codereview.dto.FullFile;
import com.codaily.codereview.dto.ManualCodeReviewRequestDto;
import com.codaily.codereview.entity.ChangeType;
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.dto.CodeReviewItemDto;
import com.codaily.codereview.dto.ReviewItemDto;
import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.codereview.repository.CodeReviewItemRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    private final ProjectRepositoriesRepository projectRepositoriesRepository;
    @Qualifier("githubWebCLient")
    private final WebClient githubWebClient;
    private final CodeCommitRepository codeCommitRepository;
    private final ProjectRepositoriesService projectRepositoriesService;
    private final UserRepository userRepository;
    private final CodeReviewItemRepository codeReviewItemRepository;
    record Key(String category, String checklistItem) {}


    @Value("${github.api-url}")
    private String githubApiUrl;

    @Value("${app.url.webhook}")
    private String webhookUrl;

    @Value("${app.url.ai}")
    private String aiUrl;

    @Value("${internal.ai-base-url}")
    private String internalAiBaseUrl;

    @Override
    public void handlePushEvent(WebhookPayload payload, Long userId) {
        List<WebhookPayload.Commit> commits = payload.getCommits();
        String accessToken = userRepository.findById(userId)
                .map(User::getGithubAccessToken)
                .orElse(null);

        for (WebhookPayload.Commit commit : commits) {
            log.info("커밋: {}", commit.getId());
            log.info("메시지: {}", commit.getMessage());
            log.info("추가된 파일: {}", commit.getAdded());
            log.info("수정된 파일: {}", commit.getModified());
            log.info("삭제된 파일: {}", commit.getRemoved());

            String fullName = payload.getRepository().getFull_name();
            String[] parts = fullName.trim().split("/");
            String repoOwner = parts[0];
            String repoName = parts[1];

            String ts = commit.getTimestamp(); // "2025-08-11T21:59:41+09:00"
            // 1) 오프셋 포함 파싱
            OffsetDateTime odt = OffsetDateTime.parse(ts);
            LocalDateTime utcTime = LocalDateTime.ofInstant(odt.toInstant(), ZoneOffset.UTC);

            ProjectRepositories repositories = projectRepositoriesService.getRepoByName(repoName);

            CodeCommit entity = CodeCommit.builder()
                            .commitHash(commit.getId())
                            .author(payload.getSender().getLogin())
                            .project(repositories.getProject())
                            .message(commit.getMessage())
                            .committedAt(utcTime).build();

            Long commitId = codeCommitRepository.save(entity).getCommitId();

            String commitBranch = payload.getRef().replace("refs/heads/", "");

            CommitInfoDto commitInfoDto = CommitInfoDto.builder().repoName(repoName).repoOwner(repoOwner).build();

            Long projectId = repositories.getProject().getProjectId();

            List<DiffFile> diffFiles = getDiffFilesFromCommit(repoOwner, repoName, commit.getId(), accessToken);

            if(diffFiles.isEmpty()) {
                log.info("변경된 파일이 없습니다. 코드리뷰를 생략합니다.");
                continue;
            }

            sendDiffFilesToPython(projectId, commitId, commit.getId(), commit.getMessage(), diffFiles, userId, commitInfoDto, commitBranch);
        }
    }

    @Override
    public List<DiffFile> getDiffFilesFromCommit(String repoOwner, String repoName, String commitHash, String accessToken) {
        String apiUrl = String.format(
                "https://api.github.com/repos/%s/%s/commits/%s",
                repoOwner, repoName, commitHash
        );
        HttpHeaders headers = new HttpHeaders();
        log.info("Webhook commit.url = {}", apiUrl);

        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("User-Agent", "codaily-bot");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, JsonNode.class);

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
                .baseUrl(internalAiBaseUrl) // Python 서버 전용
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
                .accessToken(userRepository.findById(userId)
                        .map(User::getGithubAccessToken)
                        .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다.")))
                .commitInfoDto(commitInfoDto)
                .forceDone(false)
                .build();

        webClient.post()
                .uri("/feature-inference")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.info("Python 서버로 diffFiles 전송 성공"))
                .doOnError(error -> log.error("전송 실패", error))
                .subscribe(); // 비동기 실행 (subscribe 없으면 실행 안됨)
    }

    @Override
    public void sendManualCompleteToPython(Long projectId, Long userId, Long featureId) {
        WebClient webClient = WebClient.builder()
                .baseUrl(aiUrl) // Python 서버 전용
                .build();
        FeatureItem featureItem = featureItemRepository.getFeatureItemByFeatureId(featureId);

        // 그냥 코드리뷰아이템들 불러와서 요약만 요청하는 걸로
        List<CodeReviewItemDto> codeReviewItems =
                codeReviewItemRepository.findByFeatureItem_FeatureId(featureId)
                        .stream()
                        // (선택) NPE 방지: checklist 없으면 스킵
                        .filter(cri -> cri.getFeatureItemChecklist() != null)
                        // 1) 카테고리 + 체크리스트 아이템으로 그룹핑
                        .collect(Collectors.groupingBy(
                                cri -> new Key(cri.getCategory(), cri.getFeatureItemChecklist().getItem()),
                                // 2) 각 그룹에 ReviewItemDto 리스트로 매핑
                                Collectors.mapping(
                                        cri -> new ReviewItemDto(
                                                cri.getFilePath(),
                                                cri.getLineRange(),
                                                cri.getSeverity(),
                                                cri.getMessage()
                                        ),
                                        Collectors.toList()
                                )
                        ))
                        // 3) 그룹 → CodeReviewItemDto로 변환
                        .entrySet().stream()
                        .map(e -> CodeReviewItemDto.builder()
                                .category(e.getKey().category())
                                .checklistItem(e.getKey().checklistItem())
                                .items(e.getValue())
                                .build()
                        )
                        .toList();

        ManualCodeReviewRequestDto requestDto = ManualCodeReviewRequestDto.builder()
                .projectId(projectId).featureName(featureItem.getTitle()).items(codeReviewItems).build();

        webClient.post()
                .uri("/feature-inference")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.info(" Python 서버로 코드리뷰 요청 전송 성공"))
                .doOnError(error -> log.error("전송 실패", error))
                .subscribe(); // 비동기 실행 (subscribe 없으면 실행 안됨)
    }

    @Override
    public List<FullFile> getFullFilesFromCommit(String commitHash, Long projectId, Long userId, String repoOwner, String repoName) {
        String accessToken = userRepository.findById(userId)
                .map(User::getGithubAccessToken)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        String commitUrl = String.format("%s/repos/%s/%s/commits/%s", githubApiUrl, repoOwner, repoName, commitHash);
        Mono<Map<String, Object>> responseMono = githubWebClient.get()
                .uri(commitUrl)
                .headers(h -> {
                    h.set("Authorization", "token " + accessToken);
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
                        h.set("Authorization", "token " + accessToken);
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

    @Override
    @Transactional
    public void removeAllHooksForUser(Long userId) {
        String token = userRepository.findById(userId)
                .map(User::getGithubAccessToken)
                .orElse(null);
        String repoOwner = userRepository.findById(userId)
                .map(User::getGithubAccount)
                .orElse(null);
        if (token == null || token.isBlank()) return; // 토큰 없으면 패스

        // 유저가 연결해둔 리포 목록
        List<ProjectRepositories> links =
                projectRepositoriesRepository.findByProject_User_UserId(userId);

        for (ProjectRepositories link : links) {
            String repo  = link.getRepoName();    // 엔티티에 맞게 필드명 조정

            try {
                // 1) 훅 목록 조회
                List<Map<String, Object>> hooks = githubWebClient.get()
                        .uri("{api}/repos/{owner}/{repo}/hooks",
                                Map.of("api", githubApiUrl, "owner", repoOwner, "repo", repo))
                        .headers(h -> {
                            h.setBearerAuth(token);
                            h.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                        })
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                        .block();

                if (hooks == null) continue;

                // 2) 우리 콜백 URL과 매칭되는 훅만 삭제
                for (Map<String, Object> hook : hooks) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) hook.get("config");

                    String url = config != null ? String.valueOf(config.get("url")) : null;
                    if (url == null) continue;

                    // 정확 일치 또는 startsWith 등 필요에 맞게 비교
                    if (url.equals(webhookUrl) || url.startsWith(webhookUrl)) {
                        Object idObj = hook.get("id");
                        if (idObj == null) continue;
                        long hookId = (idObj instanceof Number) ? ((Number) idObj).longValue()
                                : Long.parseLong(String.valueOf(idObj));

                        githubWebClient.delete()
                                .uri("{api}/repos/{owner}/{repo}/hooks/{id}",
                                        Map.of("api", githubApiUrl, "owner", repoOwner, "repo", repo, "id", hookId))
                                .headers(h -> h.setBearerAuth(token))
                                .retrieve()
                                .toBodilessEntity()
                                .block();

                        log.info("웹훅 삭제 완료: {}/{} (hookId={})", repoOwner, repo, hookId);
                    }
                }
            } catch (Exception e) {
                log.warn("웹훅 삭제 실패: {}/{} userId={}", repoOwner, repo, userId, e);
            }
        }
    }



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
        String token = userRepository.findById(userId)
                .map(User::getGithubAccessToken)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

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

