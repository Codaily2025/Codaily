package com.codaily.auth.service;


import com.codaily.auth.entity.User;
import com.codaily.common.git.dto.GithubFetchProfileResponse;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface UserService {

    void linkGithub(Long userId, GithubFetchProfileResponse profile, String accessToken);

    User findById(Long userId);

    String getGithubAccessToken(Long userId);

    String getGithubUsername(Long userId);

    Set<String> getUserTechStack(Long userId);

    void syncGithubTechStack(Long userId, Set<String> githubTechnologies);

    void updateCustomTechStack(Long userId, Set<String> technologies);

    void unlinkGithub(Long userId);
}
