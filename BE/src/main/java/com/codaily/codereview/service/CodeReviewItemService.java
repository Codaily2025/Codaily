package com.codaily.codereview.service;

import com.codaily.codereview.entity.CodeReviewItem;

import java.util.List;

public interface CodeReviewItemService {
    List<CodeReviewItem> getCodeReviewItemById(Long featureId);
}
