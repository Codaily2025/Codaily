package com.codaily.common.git.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class GithubServiceImpl implements GithubService {

    private final WebClient webClient = WebClient.create();

    public Mono<String> createRepository(String accessToken, String repoName) {
        return webClient.post()
                .uri("https://api.github.com/user/repos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(Map.of("name", repoName, "private", false))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(repo -> (String) repo.get("html_url"));
    }

    public Mono<Map<String, Object>> getRepositoryInfo(String accessToken, String owner, String repoName) {
        log.info("Fetching repository info for: " + owner + "/" + repoName);
        return webClient.get()
                .uri("https://api.github.com/repos/" + owner + "/" + repoName)  // owner와 repoName을 사용
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    public Mono<Map<String, Object>> fetchUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://api.github.com/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }

    public Mono<List<Map<String, Object>>> getUserRepositories(String accessToken) {
        return webClient.get()
                .uri("https://api.github.com/user/repos?type=owner&sort=updated&per_page=100")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
    }

    public Mono<Map<String, Integer>> getRepositoryLanguages(String accessToken, String owner, String repoName) {
        return webClient.get()
                .uri("https://api.github.com/repos/" + owner + "/" + repoName + "/languages")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Integer>>() {
                })
                .onErrorReturn(new HashMap<>())
                .doOnNext(languages -> log.debug("Languages for {}/{}: {}", owner, repoName, languages));
    }

    public Mono<Set<String>> getAllTechStack(String accessToken, String username) {
        Set<String> initialSet = new HashSet<>();

        return getUserRepositories(accessToken)
                .flatMapMany(repos -> Flux.fromIterable(repos))
                .cast(Map.class)
                .flatMap(repo -> {
                    String repoName = (String) repo.get("name");
                    return getRepositoryLanguages(accessToken, username, repoName); // Mono<Map<String, Integer>>
                })
                .reduce(initialSet, (totalSet, repoLanguages) -> {
                    totalSet.addAll(repoLanguages.keySet());
                    return totalSet;
                })
                .doOnNext(techStack -> log.info("Total tech stack: {}", techStack));
    }

    public Mono<List<Map<String, Object>>> getUserCommits(String accessToken, String username, String since) {
        return getUserRepositories(accessToken)
                .flatMapMany(repos -> Flux.fromIterable(repos))
                .cast(Map.class)
                .flatMap(repo -> {
                    String repoName = (String) repo.get("name");
                    String fullName = (String) repo.get("full_name");

                    return webClient.get()
                            .uri("https://api.github.com/repos/" + fullName + "/commits?author=" + username + "&since=" + since)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                            })
                            .onErrorReturn(new ArrayList<>())
                            .flatMapMany(Flux::fromIterable);
                })
                .collectList()
                .doOnNext(commits -> log.info("Found {} commits since {}", commits.size(), since));
    }

    public Mono<Map<String, Object>> getCommitActivity(String accessToken, String username) {
        String oneYearAgo = LocalDateTime.now().minusYears(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

        return getUserCommits(accessToken, username, oneYearAgo)
                .map(commits -> {
                    Map<String, Integer> dailyCommits = commits.stream()
                            .collect(Collectors.groupingBy(
                                    commit -> {
                                        Map<String, Object> commitInfo = (Map<String, Object>) commit.get("commit");
                                        Map<String, Object> author = (Map<String, Object>) commitInfo.get("author");
                                        String dateStr = (String) author.get("date");
                                        return dateStr.substring(0, 10);
                                    },
                                    Collectors.summingInt(commit -> 1)
                            ));

                    // 현재 스트릭 계산
                    int currentStreak = calculateCurrentStreak(dailyCommits);

                    Map<String, Object> result = new HashMap<>();
                    result.put("totalCommits", commits.size());
                    result.put("dailyCommits", dailyCommits);
                    result.put("currentStreak", currentStreak);

                    return result;
                });
    }

    private int calculateCurrentStreak(Map<String, Integer> dailyCommits) {
        LocalDateTime today = LocalDateTime.now();
        int streak = 0;

        for (int i = 0; i <= 365; i++) {
            String date = today.minusDays(i).toLocalDate().toString();
            if (dailyCommits.containsKey(date) && dailyCommits.get(date) > 0) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }
}
