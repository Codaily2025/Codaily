package com.codaily.common.git.service;

import com.codaily.auth.entity.User;
import com.codaily.auth.service.UserService;
import com.codaily.codereview.dto.*;
import com.codaily.codereview.entity.ChangeType;
import com.codaily.codereview.entity.CodeCommit;
import com.codaily.codereview.repository.CodeCommitRepository;
import com.codaily.codereview.repository.FeatureItemChecklistRepository;
import com.codaily.common.git.WebhookPayload;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.ProjectRepositories;
import com.codaily.project.repository.FeatureItemRepository;
import com.codaily.project.repository.ProjectRepositoriesRepository;
import com.codaily.project.service.ProjectRepositoriesService;
import com.codaily.project.service.ProjectService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
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
    private final FeatureItemChecklistRepository featureItemChecklistRepository;
    private final ProjectService projectService;
    private final GithubService githubService;
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
                            .fieldCommittedAt(LocalDateTime.parse(commit.getTimestamp())).build();

            Long commitId = codeCommitRepository.save(entity).getCommitId();
            Long projectId = repositories.getProject().getProjectId();

            sendDiffFilesToPython(projectId, commitId, commit.getId(), diffFiles);
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
    public void sendDiffFilesToPython(Long projectId,
                                      Long commitId,
                                      String commitHash,
                                      List<DiffFile> diffFiles) {

        String url = "http://localhost:8000/api/feature-inference";

        List<FeatureItem> featureItems = featureItemRepository.findByProject_ProjectId(projectId);
        List<String> availableFeatures = featureItems.stream()
                .map(FeatureItem::getTitle)
                .toList();

        FeatureInferenceRequestDto requestDto = FeatureInferenceRequestDto.builder()
                .projectId(projectId)
                .commitId(commitId)
                .commitHash(commitHash)
                .diffFiles(diffFiles)
                .availableFeatures(availableFeatures)
                .build();


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<FeatureInferenceRequestDto> entity = new HttpEntity<>(requestDto, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("✅ Python 서버로 diffFiles 전송 성공");
        } else {
            log.error("❌ 전송 실패: {}", response.getBody());
        }
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


//    @Override
//    public void sendChecklistEvaluationRequest(Long projectId, Long featureId, String featureName,
//                                               List<FullFile> fullFiles, List<ChecklistItemDto> checklistItems) {
//        String url = "http://localhost:8000/api/checklist-evaluation";
//
//        ChecklistEvaluationRequestDto requestDto = ChecklistEvaluationRequestDto.builder()
//                .projectId(projectId)
//                .featureId(featureId)
//                .featureName(featureName)
//                .fullFiles(fullFiles)
//                .checklist(checklistItems)
//                .build();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<ChecklistEvaluationRequestDto> entity = new HttpEntity<>(requestDto, headers);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            log.info("✅ checklist 평가 요청 전송 성공");
//        } else {
//            log.error("❌ checklist 평가 요청 실패: {}", response.getBody());
//        }
//    }
//
//    @Override
//    public void sendCodeReviewItemRequest(ChecklistEvaluationResponseDto responseDto) {
//        String url = "http://localhost:8000/api/code-review/items";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<ChecklistEvaluationResponseDto> entity = new HttpEntity<>(responseDto, headers);
//        RestTemplate restTemplate = new RestTemplate();
//
//        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            log.info("✅ code-review 항목 요청 전송 성공");
//        } else {
//            log.error("❌ code-review 요청 실패: {}", response.getBody());
//        }
//    }


//    @Override
//    public List<FullFile> getFullFilesFromCommit(WebhookPayload.Commit commit, String owner, String repo, String token) {
//        List<String> targetFiles = new ArrayList<>();
//        targetFiles.addAll(commit.getAdded());
//        targetFiles.addAll(commit.getModified());
//
//        List<FullFile> fullFiles = new ArrayList<>();
//
//        for (String filePath : targetFiles) {
//            String rawUrl = String.format(
//                    "https://raw.githubusercontent.com/%s/%s/%s/%s",
//                    owner, repo, commit.getId(), filePath
//            );
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(token);
//            HttpEntity<Void> entity = new HttpEntity<>(headers);
//            RestTemplate restTemplate = new RestTemplate();
//
//            try {
//                ResponseEntity<String> response = restTemplate.exchange(rawUrl, HttpMethod.GET, entity, String.class);
//                if (response.getStatusCode().is2xxSuccessful()) {
//                    fullFiles.add(new FullFile(filePath, response.getBody()));
//                }
//            } catch (Exception e) {
//                log.warn("❌ 파일 로딩 실패: {}", filePath);
//            }
//        }
//
//        return fullFiles;
//    }

//    public List<String> getChecklistByFeatureName(String featureName) {
//        return featureItemChecklistRepository.findByFeatureTitle(featureName)
//                .stream()
//                .map(FeatureItemChecklist::getItem)
//                .toList();
//    }

}
