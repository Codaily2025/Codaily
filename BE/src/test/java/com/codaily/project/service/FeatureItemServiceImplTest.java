package com.codaily.project.service;

import com.codaily.auth.entity.User;
import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.entity.Project;
import com.codaily.project.entity.Specification;
import com.codaily.project.repository.FeatureItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FeatureItemServiceImplTest {

    private FeatureItemRepository featureItemRepository;
    private FeatureItemServiceImpl featureItemService;

    @BeforeEach
    void setUp() {
        featureItemRepository = mock(FeatureItemRepository.class);
        featureItemService = new FeatureItemServiceImpl(null,null,featureItemRepository,null, null, null); // projectRepo, specRepo는 null로 둠 (사용 안함)
    }

    @Test
    void updateFeatureItem_기능정보가_정상적으로_수정된다() {
        // given
        Long featureId = 123123123L;
        FeatureItem existingItem = FeatureItem.builder()
                .featureId(featureId)
                .title("이전 제목")
                .description("이전 설명")
                .estimatedTime(1.0)
                .priorityLevel(3)
                .build();

        FeatureSaveItem updateRequest = FeatureSaveItem.builder()
                .id(featureId)
                .title("수정된 제목")
                .description("수정된 설명")
                .estimatedTime(5.0)
                .priorityLevel(9)
                .build();

        when(featureItemRepository.findById(featureId)).thenReturn(Optional.of(existingItem));

        // when
        featureItemService.updateFeatureItem(updateRequest);

        // then
        ArgumentCaptor<FeatureItem> captor = ArgumentCaptor.forClass(FeatureItem.class);
        verify(featureItemRepository, times(1)).findById(featureId);
        assertEquals("수정된 제목", existingItem.getTitle());
        assertEquals("수정된 설명", existingItem.getDescription());
        assertEquals(5, existingItem.getEstimatedTime());
        assertEquals(9, existingItem.getPriorityLevel());
    }

    @Test
    void updateFeatureItem_해당기능이_존재하지_않으면_예외() {
        // given
        Long featureId = 123123123L;
        FeatureSaveItem updateRequest = FeatureSaveItem.builder()
                .id(featureId)
                .title("제목")
                .description("설명")
                .estimatedTime(3.0)
                .priorityLevel(7)
                .build();

        when(featureItemRepository.findById(featureId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            featureItemService.updateFeatureItem(updateRequest);
        });

        verify(featureItemRepository, times(1)).findById(featureId);
    }

    @Test
    void calculateTotalEstimatedTime_하위기능합계가_정상적으로_반환된다() {
        // given
        Long specId = 123123123L;

        User dummyUser = User.builder()
                .userId(123123123L)
                .email("test@example.com")
                .nickname("tester")
                .socialProvider("github")
                .role(User.Role.USER)
                .build();

        Specification spec = Specification.builder()
                .specId(specId)
                .title("사양서 제목")
                .content("사양 내용")
                .format("json")
                .priorityLevel(1)
                .build();

        Project project = Project.builder()
                .projectId(1L)
                .title("프로젝트 A")
                .specification(spec)
                .user(dummyUser)
                .updatedAt(LocalDateTime.now())
                .build();

        FeatureItem parentFeature = FeatureItem.builder()
                .featureId(123123123L)
                .title("주 기능 A")
                .specification(spec)
                .project(project)
                .build();

        FeatureItem child1 = FeatureItem.builder()
                .featureId(123123124L)
                .title("하위 기능 1")
                .estimatedTime(5.0)
                .parentFeature(parentFeature)
                .specification(spec)
                .project(project)
                .build();

        FeatureItem child2 = FeatureItem.builder()
                .featureId(123123125L)
                .title("하위 기능 2")
                .estimatedTime(12.0)
                .parentFeature(parentFeature)
                .specification(spec)
                .project(project)
                .build();

        // Repository가 SUM 결과를 17로 리턴하도록 설정
        when(featureItemRepository.getTotalEstimatedTimeBySpecId(specId))
                .thenReturn(17);

        // when
        int totalTime = featureItemService.calculateTotalEstimatedTime(specId);

        // then
        assertEquals(17, totalTime);
        verify(featureItemRepository, times(1)).getTotalEstimatedTimeBySpecId(specId);
    }


    @Test
    void calculateTotalEstimatedTime_하위기능이_없으면_0을_반환한다() {
        // given
        Long specId = 123123123L;

        // DB에서 SUM 결과가 없으면 null 반환될 수 있음
        when(featureItemRepository.getTotalEstimatedTimeBySpecId(specId))
                .thenReturn(null);

        // when
        int totalTime = featureItemService.calculateTotalEstimatedTime(specId);

        // then
        assertEquals(0, totalTime);
        verify(featureItemRepository, times(1)).getTotalEstimatedTimeBySpecId(specId);
    }
}