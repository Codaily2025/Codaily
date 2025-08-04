package com.codaily.project.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.codaily.project.dto.FeatureSaveItem;
import com.codaily.project.entity.FeatureItem;
import com.codaily.project.repository.FeatureItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import static org.mockito.Mockito.*;

class FeatureItemServiceImplTest {

    private FeatureItemRepository featureItemRepository;
    private FeatureItemServiceImpl featureItemService;

    @BeforeEach
    void setUp() {
        featureItemRepository = mock(FeatureItemRepository.class);
        featureItemService = new FeatureItemServiceImpl(null, null, featureItemRepository); // projectRepo, specRepo는 null로 둠 (사용 안함)
    }

    @Test
    void updateFeatureItem_기능정보가_정상적으로_수정된다() {
        // given
        Long featureId = 123123123L;
        FeatureItem existingItem = FeatureItem.builder()
                .featureId(featureId)
                .title("이전 제목")
                .description("이전 설명")
                .estimatedTime(1)
                .priorityLevel(3)
                .build();

        FeatureSaveItem updateRequest = FeatureSaveItem.builder()
                .id(featureId)
                .title("수정된 제목")
                .description("수정된 설명")
                .estimatedTime(5)
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
                .estimatedTime(3)
                .priorityLevel(7)
                .build();

        when(featureItemRepository.findById(featureId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            featureItemService.updateFeatureItem(updateRequest);
        });

        verify(featureItemRepository, times(1)).findById(featureId);
    }
}