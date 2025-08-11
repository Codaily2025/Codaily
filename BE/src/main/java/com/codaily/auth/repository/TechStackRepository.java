package com.codaily.auth.repository;

import com.codaily.auth.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    Optional<TechStack> findByUserUserIdAndNameAndIsCustomTrue(Long userId, String name);

    @Query("SELECT ts.name FROM TechStack ts WHERE ts.user.userId = :userId")
    Set<String> findTechnologiesByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TechStack ts WHERE ts.user.userId = :userId")
    void deleteByUserUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TechStack ts WHERE ts.user.userId = :userId AND ts.isCustom = false")
    void deleteByUserUserIdAndIsCustomFalse(Long userId);
}
