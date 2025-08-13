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







