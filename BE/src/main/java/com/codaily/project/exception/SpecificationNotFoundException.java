package com.codaily.project.exception;

import com.codaily.global.exception.BusinessException;
import com.codaily.global.exception.ErrorCode;

public class SpecificationNotFoundException extends BusinessException {
    public SpecificationNotFoundException(Long specId) {
        super(ErrorCode.SPECIFICATION_NOT_FOUND, "명세서를 찾을 수 없습니다. ID: " + specId);
    }
}
