package com.codaily.auth.repository;

import com.codaily.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialIdAndSocialProvider(String socialId, String socialProvider);

    boolean existsByGithubAccount(String githubAccount);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT u.nickname FROM User u WHERE u.userId = :userId")
    Optional<String> findNicknameByUserId(Long userId);

    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByUserId(Long userId);

    @Query(value="select * from users where user_id=:id", nativeQuery=true)
    User findNativeAsEntity(@Param("id") Long userId);

    Optional<User> findByGithubAccount(String githubAccount);
}
