package com.codaily.project.service;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.dto.ProductivityChartResponse;
import com.codaily.project.dto.ProductivityDetailResponse;

public interface ProductivityService {
    ProductivityCalculateResponse calculateProductivity(ProductivityCalculateRequest request);
    ProductivityChartResponse getProductivityChart(Long userId, Long projectId, String period, String startDate, String endDate);

    // ✅ 이 메서드가 반드시 있어야 함
    ProductivityDetailResponse getProductivityDetail(Long userId, Long projectId, String date);
}


