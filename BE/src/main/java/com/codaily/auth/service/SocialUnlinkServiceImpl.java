package com.codaily.auth.service;

import com.codaily.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class SocialUnlinkServiceImpl implements SocialUnlinkService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${GOOGLE.CLIENT_ID}")
    String googleClientId;
    @Value("${GOOGLE.CLIENT_SECRET}")
    String googleClientSecret;

    @Value("${NAVER.CLIENT_ID}")
    String naverClientId;
    @Value("${NAVER.CLIENT_SECRET}")
    String naverClientSecret;

    @Value("${KAKAO_REST_API_KEY}")
    String kakaoAdminKey; // Admin Key

    @Value("${GITHUB.CLIENT_ID}")
    String githubClientId;
    @Value("${GITHUB.CLIENT_SECRET}")
    String githubClientSecret;

    @Override
    public void unlinkSocial(User user) {
        String provider = Optional.ofNullable(user.getSocialProvider()).orElse("").toLowerCase();
        switch (provider) {
            case "google" -> revokeGoogle(user);
            case "naver" -> revokeNaver(user);
            case "kakao" -> unlinkKakao(user);
            default -> { /* noop */ }
        }
    }

    private void revokeGoogle(User user) {
        String token = Optional.ofNullable(user.getSocialRefreshToken())
                .orElse(user.getSocialAccessToken());
        if (isBlank(token)) return;

        // POST https://oauth2.googleapis.com/revoke (x-www-form-urlencoded: token=...)
        webClient.post()
                .uri("https://oauth2.googleapis.com/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("token", token))
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> Mono.empty())
                .block();
    }

    private void revokeNaver(User user) {
        String accessToken = user.getSocialAccessToken();
        if (isBlank(accessToken)) return;

        // GET https://nid.naver.com/oauth2.0/token?grant_type=delete&client_id=...&client_secret=...&access_token=...&service_provider=NAVER
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https").host("nid.naver.com").path("/oauth2.0/token")
                        .queryParam("grant_type", "delete")
                        .queryParam("client_id", naverClientId)
                        .queryParam("client_secret", naverClientSecret)
                        .queryParam("access_token", accessToken)
                        .queryParam("service_provider", "NAVER")
                        .build())
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> Mono.empty())
                .block();
    }

    private void unlinkKakao(User user) {
        String kakaoUserId = user.getSocialId(); // Kakao는 숫자 문자열
        if (isBlank(kakaoUserId)) return;

        // POST https://kapi.kakao.com/v1/user/unlink (Admin Key)
        webClient.post()
                .uri("https://kapi.kakao.com/v1/user/unlink")
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoAdminKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("target_id_type", "user_id")
                        .with("target_id", kakaoUserId))
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> Mono.empty())
                .block();
    }

    // 공통 WebClient (UA/Accept 고정)
    private WebClient ghClient() {
        return webClient.mutate()
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "CodailyApp")
                .build();
    }

    private Mono<ClientResponse> deleteGrant(String clientId, String clientSecret, String token) {
        return ghClient().method(HttpMethod.DELETE)
                .uri("https://api.github.com/applications/{client_id}/grant", clientId)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, basicAuthHeader(githubClientId, githubClientSecret)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("access_token", token))
                .exchangeToMono(Mono::just); // 상태/헤더를 그대로 보려면 retrieve 말고 exchangeToMono 사용
    }

    private Mono<ClientResponse> deleteToken(String clientId, String clientSecret, String token) {
        return ghClient().method(HttpMethod.DELETE)
                .uri("https://api.github.com/applications/{client_id}/token", clientId)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, basicAuthHeader(githubClientId, githubClientSecret)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("access_token", token))
                .exchangeToMono(Mono::just);
    }

    private Mono<ClientResponse> checkToken(String clientId, String clientSecret, String token) {
        // 이 토큰이 client_id 앱의 것인지 확인 (200이면 매칭, 404면 불일치)
        return ghClient().post()
                .uri("https://api.github.com/applications/{client_id}/token", clientId)
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, basicAuthHeader(clientId, clientSecret)))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("access_token", token))
                .exchangeToMono(Mono::just);
    }

    private String basicAuthHeader(String clientId, String clientSecret) {
        String raw = clientId + ":" + clientSecret;
        String b64 = java.util.Base64.getEncoder()
                .encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        log.info("[GH AUTH] client_id(head)={}, basic_auth(head)={}",
                clientId.substring(0, Math.min(6, clientId.length())),
                b64.substring(0, 10) + "..."); // 마스킹 로그
        return "Basic " + b64;
    }

    @Override
    @Transactional
    public void revokeGithub(User user) {
        log.info("user: {}", user);
        String token = user.getGithubAccessToken();
        log.info("token: {}", token);
        if (token == null || token.isBlank()) return;
        ClientResponse check = checkToken(githubClientId, githubClientSecret, token)
                .subscribeOn(Schedulers.boundedElastic())
                .block(Duration.ofSeconds(10));
        logHeaders("CHECK", check);

        ClientResponse g = deleteGrant(githubClientId, githubClientSecret, token)
                .subscribeOn(Schedulers.boundedElastic())
                .block(Duration.ofSeconds(10));
        logHeaders("GRANT", g);

        ClientResponse t = deleteToken(githubClientId, githubClientSecret, token)
                .subscribeOn(Schedulers.boundedElastic())
                .block(Duration.ofSeconds(10));
        logHeaders("TOKEN", t);

        user.setGithubAccessToken(null);
        user.setGithubScope(null);
    }

    private void logHeaders(String tag, ClientResponse r) {
        var h = r.headers().asHttpHeaders();
        log.info("[{}] status={}, ratelimit={}, reqId={}",
                tag, r.statusCode(),
                h.getFirst("X-RateLimit-Limit"),
                h.getFirst("X-GitHub-Request-Id"));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

