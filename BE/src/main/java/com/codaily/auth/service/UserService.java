package com.codaily.auth.service;


import com.codaily.auth.entity.User;
import com.codaily.common.git.dto.GithubFetchProfileResponse;

public interface UserService {

    void linkGithub(Long userId, GithubFetchProfileResponse profile, String accessToken);

    User findById(Long userId);

    String getGithubAccessToken(Long userId);
}
