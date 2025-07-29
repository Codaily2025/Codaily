package com.codaily.project.service;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;

public interface ProductivityService {
    ProductivityCalculateResponse calculateProductivity(ProductivityCalculateRequest request);
}
