package com.codaily.project.service;

import com.codaily.project.dto.*;
import java.time.LocalDate;

public interface FeatureDetailService {

    //기능 상세 정보 조회
    FeatureDetailResponse getFeatureDetail(Long projectId, Long featureId, Long userId);

    //기능 정보 수정
    FeatureDetailResponse updateFeature(Long projectId, Long featureId, FeatureUpdateRequest request, Long userId);

    //하위 기능 생성 (같은 프로젝트에 부모 기능을 가진 새로운 기능 생성)
    SubFeatureCreateResponse createSubFeature(Long projectId, Long parentFeatureId, SubFeatureCreateRequest request, Long userId);

    //캘린더 특정 날짜의 기능 정보 조회
    CalendarFeatureResponse getCalendarFeatures(Long projectId, LocalDate date, Long userId);

    //기능의 하위 기능 목록 조회
    SubFeatureListResponse getSubFeatures(Long projectId, Long featureId, Long userId);

    //기능 삭제 (하위 기능들도 함께 삭제)
    void deleteFeature(Long projectId, Long featureId, Long userId);
}