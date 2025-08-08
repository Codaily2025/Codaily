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
    public List<CodeReviewItem> getCodeReviewById(Long featureId) {
        return codeReviewItemRepository.findByCodeReview_FeatureItem_Id(featureId);
    }
}
