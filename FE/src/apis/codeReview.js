// FE/src/apis/codeReview.js
import { authInstance } from './axios';

// 코드 리뷰 목록조회 API
// http://localhost:8081/api/code-review/user/{userId}
export async function fetchCodeReviewList() {
    const userId = 1; // 현재 테스트 용으로 임시로 userId 1로 설정
    // 이후에는 로그인한 현재 유저 기반으로 userId를 안 넣어도 될 수 있음
    // 향후 구현 방식에 따라 달라질 것임
    try {
        const response = await authInstance.get(`/code-review/user/${userId}`);
        console.log('가져온 코드 리뷰 목록:', response.data);
        // 원하는 결과 형식 : tags: [{text: '중간 1', type: 'medium'}, {text: '낮음 4', type: 'low'}]
        const tags = (response.data.severityCount) ? response.data.severityCount.map(item => ({
            text: item.severity + ' ' + item.count, // 예: 높음 8
            type: item.severity,
        })) : [];
        return {
            category: response.data.featureField,
            title: response.data.featureName,
            score: response.data.qualityScore,
            tags: tags,
        };
    } catch (error) {
        console.error('코드 리뷰 목록 조회 실패:', error);
        throw error;
    }
}