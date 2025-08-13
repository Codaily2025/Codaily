package com.codaily.auth.service;


import com.codaily.auth.entity.TechStack;
import com.codaily.auth.entity.User;
import com.codaily.auth.repository.TechStackRepository;
import com.codaily.auth.repository.UserRepository;
import com.codaily.common.git.dto.GithubFetchProfileResponse;
import com.codaily.common.git.service.WebhookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TechStackRepository techStackRepository;
    private final SocialUnlinkService socialUnlinkService;
    private final WebhookService webhookService;

    @Override
    public void linkGithub(Long userId, GithubFetchProfileResponse profile, String accessToken) {
        if (profile == null || accessToken == null) {
            throw new IllegalArgumentException("GitHub 정보 또는 토큰이 누락되었습니다.");
        }

        // 이미 연동된 GitHub ID인지 확인
        if (profile.getId() != null && userRepository.existsByGithubAccount(profile.getLogin())) {
            throw new IllegalStateException("이미 다른 계정과 연동된 GitHub 계정입니다.");
        }

        User user = findById(userId);

        user.setGithubAccount(profile.getLogin());
        user.setGithubProfileUrl(profile.getHtmlUrl());
        user.setGithubAccessToken(accessToken);
        user.setGithubScope(null); // scope 추출 시 여기에 넣어도 됨
        user.setTokenExpiredAt(null); // GitHub는 만료시간 제공 안 함
        user.setGithubScope(null);
        user.setGithubAccessToken(accessToken);
        user.setGithubProfileUrl(profile.getHtmlUrl());
        user.setGithubAccount(profile.getLogin());

        userRepository.save(user);
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Override
    public String getGithubAccessToken(Long userId) {
        return userRepository.findById(userId)
                .map(User::getGithubAccessToken)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));
    }

    @Override
    public String getGithubUsername(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        return user.getGithubAccount();
    }

    @Override
    public Set<String> getUserTechStack(Long userId) {
        return techStackRepository.findTechnologiesByUserId(userId);
    }

    @Override
    @Transactional
    public void syncGithubTechStack(Long userId, Set<String> githubTechnologies) {
        User user = findById(userId);

        // 기존 GitHub 자동 추가 기술스택 삭제
        techStackRepository.deleteByUserUserIdAndIsCustomFalse(userId);

        // 새로운 GitHub 기술스택 추가
        for (String tech : githubTechnologies) {
            Optional<TechStack> existing = techStackRepository.findByUserUserIdAndNameAndIsCustomTrue(userId, tech);
            if (existing.isEmpty()) {
                TechStack techStack = TechStack.builder()
                        .user(user)
                        .name(tech)
                        .isCustom(false)
                        .build();
                techStackRepository.save(techStack);
            }
        }
    }

    @Override
    @Transactional
    public void updateCustomTechStack(Long userId, Set<String> technologies) {
        User user = findById(userId);

        techStackRepository.deleteByUserUserId(userId);

        for (String tech : technologies) {
            TechStack techStack = TechStack.builder()
                    .user(user)
                    .name(tech)
                    .isCustom(true)
                    .build();
            techStackRepository.save(techStack);
        }
    }

    @Override
    public void unlinkGithub(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        user.setGithubAccessToken(null);
        user.setGithubAccount(null);
        user.setGithubProfileUrl(null);

        userRepository.save(user);
    }

    @Override
    public String getUserNickname(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 null일 수 없습니다.");
        }

        String nickname = userRepository.findNicknameByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return nickname;
    }

    @Override
    public void updateUserNickname(Long userId, String newNickname) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 null일 수 없습니다.");
        }

        if (newNickname == null || newNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        user.setNickname(newNickname.trim());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findNativeAsEntity(userId);
        log.info(userId);
        log.info(user);
        log.info("unlink github webhook");
        // 1) (선택) 깃허브 웹훅 제거 (리포는 삭제하지 않음)
        webhookService.removeAllHooksForUser(user.getUserId());
        log.info(user);
        log.info("unlink social login");
        // 2) 소셜 로그인 unlink/revoke
        socialUnlinkService.unlinkSocial(user);
        log.info(user);
        log.info("unlink github");
        // 3) GitHub 토큰 revoke (리포 보존)
        socialUnlinkService.revokeGithub(user);
        log.info(user);
        log.info("delete user");
        userRepository.delete(user);
    }
}
