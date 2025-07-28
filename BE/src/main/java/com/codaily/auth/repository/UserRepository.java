package com.codaily.auth.repository;

import com.codaily.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByGithubAccount(String githubAccount);
}
