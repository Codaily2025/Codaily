// FE/src/apis/requirementsSpecification.js
import { authInstance } from './axios';


// 프로젝트 요구사항 명세서 pdf 다운로드 api
// /api/specifications/{projectId}/document

export async function downloadSpecDocument(projectId) {
    try {
        const response = await authInstance.get(`specifications/${projectId}/document`, {
            responseType: 'blob',
            headers: {
                Accept: 'application/pdf',
            },
        });
        console.log('가져온 프로젝트 요구사항 명세서:', response);
        return response;
    } catch (error) {
        console.error('프로젝트 요구사항 명세서 조회 실패:', error);
        throw error;
    }
}

// 프로젝트 체크박스 선택, 해제 토글 패치
// /api/projects/{projectId}/specification/reduce
export async function toggleReduceFlag(projectId, field, featureId, isReduced) {
    try {
        if (field && featureId) {
            throw new Error('field와 featureId는 둘 중 하나만 존재함. 둘 다 존재하면 안됨');
        }
        
        const requestData = {
            isReduced: isReduced, // 감축 여부
        };
        
        let url = `projects/${projectId}/specification/reduce`;
        const params = {};
        
        if (field) {
            params.field = field;
        } else if (featureId) {
            params.featureId = featureId;
        }
        console.log('프로젝트 체크박스 토글 패치 요청:', params, requestData);
        // console.log('요청 URL:', url);
        // console.log('요청 파라미터:', params);
        // console.log('요청 본문:', requestData);
        const response = await authInstance.patch(url, requestData, {
            params: params
        });
        
        return response.data;
    } catch (error) {
        console.error('프로젝트 체크박스 선택, 해제 토글 패치 실패:', error);
        throw error;
    }
}




