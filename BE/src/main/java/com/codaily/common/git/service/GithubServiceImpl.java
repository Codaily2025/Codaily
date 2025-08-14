package com.codaily.common.git.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class GithubServiceImpl implements GithubService {

    private final WebClient webClient = WebClient.create();

    @Value("${app.url.webhook}")
    private String webhookUrl;

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

    @Override
    public void registerWebhook(String owner, String repo, String accessToken) {
        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/hooks";
        // 도메인 추후 수정 필요
        String targetUrl = webhookUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/vnd.github+json");

        RestTemplate restTemplate = new RestTemplate();

        try {
            // 기존 Webhook 목록 조회
            HttpEntity<Void> getRequest = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, getRequest, List.class
            );

            List<Map<String, Object>> hooks = response.getBody();

            // 2. Codaily Webhook이 이미 등록돼 있는지 확인
            boolean alreadyRegistered = hooks.stream()
                    .map(hook -> (Map<String, Object>) hook.get("config"))
                    .filter(Objects::nonNull)
                    .anyMatch(config -> targetUrl.equals(config.get("url")));

            if (alreadyRegistered) {
                log.info("이미 Codaily Webhook이 등록되어 있음: {}", repo);
                return;
            }

            // 등록된 webhook에 없으면 새로 등록
            Map<String, Object> config = Map.of(
                    "url", targetUrl,
                    "content_type", "json"
            );

            Map<String, Object> body = Map.of(
                    "name", "web",
                    "active", true,
                    "events", List.of("push"),
                    "config", config
            );

            HttpEntity<Map<String, Object>> postRequest = new HttpEntity<>(body, headers);
            ResponseEntity<String> postResponse = restTemplate.postForEntity(apiUrl, postRequest, String.class);

            if (postResponse.getStatusCode().is2xxSuccessful()) {
                log.info("Webhook 새로 등록 성공: {}", targetUrl);
            } else {
                log.warn("Webhook 등록 실패: {}", postResponse.getBody());
            }

        } catch (HttpClientErrorException e) {
            log.error("GitHub Webhook 등록 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("기타 예외 발생: {}", e.getMessage(), e);
        }
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

    @Override
    public Mono<Integer> getCommitsByDate(String accessToken, String username, LocalDate date) {
        String since = date.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
        String until = date.atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

        return getUserRepositories(accessToken)
                .flatMapMany(repos -> Flux.fromIterable(repos))
                .cast(Map.class)
                .flatMap(repo -> {
                    String fullName = (String) repo.get("full_name");

                    return webClient.get()
                            .uri("https://api.github.com/repos/" + fullName + "/commits?author=" + username +
                                    "&since=" + since + "&until=" + until)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                            })
                            .onErrorReturn(new ArrayList<>())
                            .flatMapMany(Flux::fromIterable);
                })
                .collectList()
                .map(List::size)
                .doOnNext(count -> log.debug("커밋 수 조회 - 날짜: {}, 커밋 수: {}", date, count));
    }

    @Override
    public Mono<Map<LocalDate, Integer>> getDailyCommitStats(String accessToken, String username, LocalDate startDate, LocalDate endDate) {
        String since = startDate.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
        String until = endDate.atTime(23, 59, 59).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

        return getUserCommits(accessToken, username, since)
                .map(commits -> {
                    Map<LocalDate, Integer> dailyStats = new HashMap<>();

                    // 기간 내 모든 날짜를 0으로 초기화
                    LocalDate current = startDate;
                    while (!current.isAfter(endDate)) {
                        dailyStats.put(current, 0);
                        current = current.plusDays(1);
                    }

                    // 커밋들을 날짜별로 그룹핑
                    commits.forEach(commit -> {
                        try {
                            Map<String, Object> commitInfo = (Map<String, Object>) commit.get("commit");
                            Map<String, Object> author = (Map<String, Object>) commitInfo.get("author");
                            String dateStr = (String) author.get("date");

                            LocalDate commitDate = LocalDate.parse(dateStr.substring(0, 10));
                            if (!commitDate.isBefore(startDate) && !commitDate.isAfter(endDate)) {
                                dailyStats.put(commitDate, dailyStats.getOrDefault(commitDate, 0) + 1);
                            }
                        } catch (Exception e) {
                            log.warn("커밋 날짜 파싱 실패: {}", e.getMessage());
                        }
                    });

                    return dailyStats;
                })
                .doOnNext(stats -> log.debug("일별 커밋 통계 조회 완료 - 기간: {} ~ {}, 총 {}일",
                        startDate, endDate, stats.size()));
    }
}
