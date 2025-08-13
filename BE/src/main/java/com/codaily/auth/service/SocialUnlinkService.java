package com.codaily.auth.service;

import com.codaily.auth.entity.User;

public interface SocialUnlinkService {
    void unlinkSocial(User user);
    void revokeGithub(User user);
}
