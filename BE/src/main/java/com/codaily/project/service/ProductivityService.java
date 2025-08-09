package com.codaily.project.service;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.dto.ProductivityChartResponse;
import com.codaily.project.dto.ProductivityDetailResponse;

public interface ProductivityService {
    ProductivityCalculateResponse calculateProductivity(ProductivityCalculateRequest request);

    // 단일 프로젝트 생산성 차트
    ProductivityChartResponse getProjectProductivityChart(Long userId, Long projectId, String period, String startDate, String endDate);

    // 전체 프로젝트 생산성 차트
    ProductivityChartResponse getAllProjectsProductivityChart(Long userId, String period, String startDate, String endDate);

    ProductivityDetailResponse getProductivityDetail(Long userId, Long projectId, String date);
}