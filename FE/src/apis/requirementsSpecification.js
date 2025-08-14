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
// FE/src/apis/requirementsSpecification.js에서 toggleReduceFlag 함수 수정
export async function toggleReduceFlag(projectId, field, featureId, isReduced, cascadeChildren = true) {
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
          // field 모드에서는 cascadeChildren 파라미터 무시 (항상 cascade)
      } else if (featureId) {
          params.featureId = featureId;
          params.cascadeChildren = cascadeChildren; // 추가된 파라미터
      }
      
      console.log('프로젝트 체크박스 토글 패치 요청:', params, requestData);
      console.log('요청 URL:', url);
      console.log('요청 파라미터:', params);
      console.log('요청 본문:', requestData);
      
      const response = await authInstance.patch(url, requestData, {
          params: params
      });
      
      console.log('API 응답:', response);
      return response.data;
  } catch (error) {
      console.error('프로젝트 체크박스 선택, 해제 토글 패치 실패:', error);
      throw error;
  }
}

// 요구사항 명세서 조회 API
// /api/projects/{projectId}/spec
export async function fetchSpec(projectId) {
    try {
        // console.log('요구사항 명세서 조회 api에서 받은 projectId:', projectId)
        const response = await authInstance.get(`projects/${projectId}/spec`);
        // console.log('요구사항 명세서 조회 응답:', response.data);
        return response.data;
    } catch (error) {
        console.error('요구사항 명세서 조회 실패:', error);
        throw error;
    }
}

export const finalizeSpecification = async (projectId) => {
  try {
    console.log('요구사항 명세서 확정 요청:', { projectId });
    console.log('백엔드에서 isReduced=true인 모든 기능을 삭제합니다.');
    
    const response = await authInstance.post(
      `projects/${projectId}/specification/finalize`
    );
    
    console.log('요구사항 명세서 확정 응답:', response.data);
    return response.data;
  } catch (error) {
    console.error('요구사항 명세서 확정 실패:', error);
    console.error('요구사항 명세서 확정 실패 응답:', error.response?.data);
    throw error;
  }
};


