package com.codaily.codereview.service;

import com.codaily.codereview.entity.CodeReviewItem;
import com.codaily.codereview.repository.CodeReviewItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeReviewItemServiceImpl implements CodeReviewItemService{

    private final CodeReviewItemRepository codeReviewItemRepository;

    @Override
    public List<CodeReviewItem> getCodeReviewItemById(Long featureId) {
        return codeReviewItemRepository.findByFeatureItem_FeatureId(featureId);
    }
}
