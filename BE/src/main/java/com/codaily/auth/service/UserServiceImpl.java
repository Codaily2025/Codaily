package com.codaily.auth.service;


import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.common.git.dto.GithubFetchProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
}
