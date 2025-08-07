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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );

        // 2. Project 저장
        Project project = projectRepository.save(Project.builder()
                .title("테스트 프로젝트")
                .description("설명")
                .status(Project.ProjectStatus.TODO)
                .specification(spec)
                .user(user)
                .updatedAt(LocalDateTime.now())
                .build()
        );

        // 3. FeatureItem 저장
        FeatureItem item = FeatureItem.builder()
                .title("기능명")
                .description("기능 설명")
                .estimatedTime(3.0)
                .priorityLevel(7)
                .project(project)
                .specification(spec)
                .isCustom(false)
                .build();

        FeatureItem saved = featureItemRepository.save(item);
        assertNotNull(saved.getFeatureId());
    }

    @Test
    void getTotalEstimatedTimeBySpecId_하위기능합계_정상동작확인() {
        // 0. 유저 먼저 생성
        User user = userRepository.save(User.builder()
                .email("sumtest@example.com")
                .nickname("합계유저")
                .socialProvider("test")
                .role(User.Role.USER)
                .build());

        // 1. Specification 먼저 저장
        Specification spec = specificationRepository.save(Specification.builder()
                .title("합계 명세")
                .content("명세 내용")
                .format("json")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // 2. Project 저장
        Project project = projectRepository.save(Project.builder()
                .title("합계 테스트 프로젝트")
                .description("합계용 설명")
                .status(Project.ProjectStatus.TODO)
                .specification(spec)
                .user(user)
                .updatedAt(LocalDateTime.now())
                .build());

        // 3. 주 기능 (parentFeature == null)
        FeatureItem parent = featureItemRepository.save(FeatureItem.builder()
                .title("주 기능")
                .description("주 설명")
                .estimatedTime(10.0)
                .priorityLevel(1)
                .project(project)
                .specification(spec)
                .isCustom(false)
                .build());

        // 4. 하위 기능 2개 저장 (SUM 대상)
        featureItemRepository.save(FeatureItem.builder()
                .title("하위 기능 1")
                .description("하위 1")
                .estimatedTime(4.0)
                .priorityLevel(2)
                .project(project)
                .specification(spec)
                .parentFeature(parent)
                .isCustom(false)
                .build());

        featureItemRepository.save(FeatureItem.builder()
                .title("하위 기능 2")
                .description("하위 2")
                .estimatedTime(6.0)
                .priorityLevel(2)
                .project(project)
                .specification(spec)
                .parentFeature(parent)
                .isCustom(false)
                .build());

        // when
        Integer total = featureItemRepository.getTotalEstimatedTimeBySpecId(spec.getSpecId());

        // then
        org.assertj.core.api.Assertions.assertThat(total).isEqualTo(10); // 4 + 6
    }

}