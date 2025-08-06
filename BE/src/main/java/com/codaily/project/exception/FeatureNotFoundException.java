package com.codaily.project.exception;

import com.codaily.global.exception.BusinessException;
import com.codaily.global.exception.ErrorCode;
public class FeatureNotFoundException extends BusinessException {
    public FeatureNotFoundException(Long featureId) {
        super(ErrorCode.FEATURE_NOT_FOUND, "기능을 찾을 수 없습니다. ID: " + featureId);
    }
}