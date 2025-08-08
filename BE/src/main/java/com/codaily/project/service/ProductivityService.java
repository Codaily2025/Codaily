package com.codaily.project.service;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.dto.ProductivityChartResponse;
import com.codaily.project.dto.ProductivityDetailResponse;

public interface ProductivityService {
    ProductivityCalculateResponse calculateProductivity(ProductivityCalculateRequest request);

    // 프로젝트별 그래프
    ProductivityChartResponse getProductivityChart(Long userId, Long projectId, String period, String startDate, String endDate);

    // 전체 프로젝트 합산 그래프
    ProductivityChartResponse getOverallProductivityChart(Long userId, String period, String startDate, String endDate);

    ProductivityDetailResponse getProductivityDetail(Long userId, Long projectId, String date);


}

