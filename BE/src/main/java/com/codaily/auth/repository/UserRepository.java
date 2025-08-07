package com.codaily.auth.repository;

import com.codaily.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByGithubAccount(String githubAccount);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u.nickname FROM User u WHERE u.id = :userId")
    Optional<String> findNicknameByUserId(Long userId);
}
