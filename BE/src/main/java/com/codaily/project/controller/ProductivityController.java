package com.codaily.project.controller;

import com.codaily.project.dto.ProductivityCalculateRequest;
import com.codaily.project.dto.ProductivityCalculateResponse;
import com.codaily.project.service.ProductivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productivity")
@RequiredArgsConstructor
public class ProductivityController {

    private final ProductivityService productivityService;

    @PostMapping("/calculate")
    public ProductivityCalculateResponse calculate(@RequestBody ProductivityCalculateRequest request) {
        return productivityService.calculateProductivity(request);
    }
}
