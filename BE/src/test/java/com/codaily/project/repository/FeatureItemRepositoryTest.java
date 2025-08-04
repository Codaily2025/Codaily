package com.codaily.project.repository;

import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FeatureItemRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private FeatureItemRepository featureItemRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SpecificationRepository specificationRepository;

    @Test
    void save_정상동작확인() {
        // 0. 유저 먼저 생성
        User user = userRepository.save(User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .socialProvider("test")
                .role(User.Role.USER)
                .build());

        // 1. Specification 먼저 저장
        Specification spec = specificationRepository.save(Specification.builder()
                .title("테스트 명세")
                .content("테스트 명세 내용")
                .format("json")
                        .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );

        // 2. Project 저장
        Project project = projectRepository.save(Project.builder()
                .title("테스트 프로젝트")
                .description("설명")
                .status("TODO")
                .specification(spec)
                .user(user)
                .updatedAt(LocalDateTime.now())
                .build()
        );

        // 3. FeatureItem 저장
        FeatureItem item = FeatureItem.builder()
                .title("기능명")
                .description("기능 설명")
                .estimatedTime(3)
                .priorityLevel(7)
                .project(project)
                .specification(spec)
                .isCustom(false)
                .build();

        FeatureItem saved = featureItemRepository.save(item);
        assertNotNull(saved.getFeatureId());
    }
}