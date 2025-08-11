package com.codaily.auth.service;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.auth.service.provider.GoogleOAuth;
import com.codaily.auth.service.provider.KakaoOAuth;
import com.codaily.auth.service.provider.NaverOAuth;
import com.codaily.auth.service.provider.SocialOAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request); // provider로부터 사용자 정보 획득

        String registrationId = request.getClientRegistration().getRegistrationId(); // ex: google, github, naver
        Map<String, Object> attributes = oAuth2User.getAttributes();
        SocialOAuth socialUser;
//        log.info("attributes: "+attributes);
        // OAuth2 provider별 파싱 로직 선택
        switch (registrationId) {
            case "google" -> socialUser = new GoogleOAuth(attributes);
//            case "github" -> socialUser = new GitHubOAuth(attributes);
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                socialUser = new NaverOAuth(response);
            }
            case "kakao" -> socialUser = new KakaoOAuth(attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        }

        // 기본 정보 추출
        String provider = socialUser.getProvider(); // github, google 등
        String providerId = socialUser.getProviderId();
        String email = socialUser.getEmail();
        String name = socialUser.getName();
        log.info("email: "+email);
        Optional<User> existingUser;
        if ("kakao".equals(provider)) {
            existingUser = userRepository.findBySocialIdAndSocialProvider(providerId, provider);
        } else {
            existingUser = userRepository.findByEmail(email);
        }

        User user;

        // 1. userId와 nickname 자동 생성 (ex: github_f3d9a2)
        String nickname = provider + "_" + createRandomUserNickname(20 - provider.length());

        // 2. userId 중복 방지
        while (userRepository.existsByNickname(nickname)) {
            nickname = provider + "_" + createRandomUserNickname(20 - provider.length());
        }

        // 3. 이메일 중복 여부 확인
        if (existingUser.isEmpty()) {
            log.info("{} 최초 로그인입니다. 자동 회원가입 처리", provider);

            user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .role(User.Role.USER)
                    .socialProvider(provider)
                    .socialId(providerId)
                    .githubAccount(provider.equals("github") ? socialUser.getProviderId() : null)
                    .githubProfileUrl(provider.equals("github") ? (String) attributes.get("avatar_url") : null)
                    .githubAccessToken(provider.equals("github") ? request.getAccessToken().getTokenValue() : null)
                    .githubScope(provider.equals("github") ? String.join(",", request.getAccessToken().getScopes()) : null)
                    .build();

            userRepository.save(user);
        } else if (existingUser.get().getSocialProvider().equals(provider)) {
            log.info("이미 {} 계정으로 로그인한 이력이 있습니다.", provider);
            user = existingUser.get();
        } else {
            log.warn("같은 이메일이 다른 플랫폼({})으로 가입되어 있음", existingUser.get().getSocialProvider());
            throw new OAuth2AuthenticationException("이미 다른 플랫폼으로 가입된 이메일입니다.");
        }


        // Spring Security에 사용자 인증 객체 전달
        return new PrincipalDetails(user, attributes);
    }

    private String createRandomUserNickname(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
