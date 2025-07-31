package com.codaily.management.exception;

import com.codaily.global.exception.BusinessException;
import com.codaily.global.exception.ErrorCode;

public class ProjectNotFoundException extends BusinessException {
    public ProjectNotFoundException(Long projectId){
        super(ErrorCode.PROJECT_NOT_FOUND, "프로젝트를 찾을 수 없습니다. ID: " + projectId);
    }
}
