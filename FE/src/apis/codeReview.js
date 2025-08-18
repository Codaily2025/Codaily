// FE/src/apis/codeReview.js
import { authInstance } from './axios';

// 코드 리뷰 목록조회 API
// http://localhost:8081/api/code-review/user/{userId}
export async function fetchCodeReviewList() {
    const userId = 16; // 현재 테스트 용으로 임시로 userId 1로 설정
    // 이후에는 로그인한 현재 유저 기반으로 userId를 안 넣어도 될 수 있음
    // 향후 구현 방식에 따라 달라질 것임
    try {
        const response = await authInstance.get(`code-review/user`);
        console.log('가져온 코드 리뷰 목록:', response.data);
        
        // API 응답이 배열인지 확인하고 처리
        const dataArray = Array.isArray(response.data) ? response.data : [response.data];
        
        // 각 항목을 원하는 형식으로 변환
        return dataArray.map(item => {
            const tags = (item.severityCount) ? Object.entries(item.severityCount).map(([severity, count]) => ({
                text: severity.trim() + ' ' + count, // 공백 제거 후 사용: '낮음 1', '중간 2', '높음 2'
                type: severity.trim(), // 공백 제거 후 사용: '낮음', '중간', '높음'
            })) : [];
            // 현재 createdAt 예시 : 2025-08-09T21:00:33.408579
            // 원하는 형식 : 2025/08/09 21:00
            const apiDate = item.createdAt.split('T')[0];
            const apiTime = item.createdAt.split('T')[1].slice(0, 5);
            const reviewDate = apiDate.replace(/-/g, '/');
            // 리뷰 날짜와 커밋 수 표시
            const details = `${reviewDate} ${apiTime} · 커밋 ${item.commitCounts}번`;
            return {
                details: details,
                projectId: item.projectId,
                projectName: item.projectName,
                id: item.featureId,
                // reviewId: item.reviewId,
                // featureId: item.featureId,
                category: item.featureField,
                title: item.featureName,
                score: item.qualityScore,
                tags: tags,
                scoreColor: item.qualityScore >= 80 ? 'green' : item.qualityScore >= 60 ? 'orange' : 'red',
            };
        });
    } catch (error) {
        console.error('코드 리뷰 목록 조회 실패:', error);
        throw error;
    }
}

// 실제로 API로 받은 데이터 예시
// [
//   {
//     projectId: 1,
//     featureId: 2,
//     featureField: 'Authentication',
//     featureName: '사용자 회원가입 기능',
//     qualityScore: 85,
//     severityCount: {
//       high: 8,
//       medium: 4
//     }
//   },
//   {
//     projectId: 1,
//     featureId: 3,
//     featureField: 'Product',
//     featureName: '상품 카탈로그',
//     qualityScore: 85,
//     severityCount: {
//     } // 비어있는 상태로도 옴
//   }
// ]

// 프로젝트 옵션 생성 함수
export function generateProjectOptions(codeReviewData) {
    if (!Array.isArray(codeReviewData) || codeReviewData.length === 0) {
        return [
            {
                id: null,
                name: '모든 프로젝트',
                features: ['전체']
            }
        ];
    }

    // 프로젝트별로 데이터 그룹화
    const projectMap = new Map();
    
    codeReviewData.forEach(item => {
        const projectId = item.projectId;
        const featureName = item.category;
        
        if (!projectMap.has(projectId)) {
            projectMap.set(projectId, {
                id: projectId,
                name: `${item.projectName}`,
                features: ['전체']
            });
        }
        
        // 중복 제거하면서 기능 추가
        const project = projectMap.get(projectId);
        if (!project.features.includes(featureName)) {
            project.features.push(featureName);
        }
    });

    // 모든 프로젝트의 기능을 수집
    const allFeatures = ['전체'];
    codeReviewData.forEach(item => {
        if (!allFeatures.includes(item.category)) {
            allFeatures.push(item.category);
        }
    });

    // 결과 배열 생성
    const result = [
        {
            id: null,
            name: '모든 프로젝트',
            features: allFeatures
        },
        ...Array.from(projectMap.values())
    ];

    return result;
}

// 특정 리뷰의 상세 정보를 가져오는 함수
// http://localhost:8081/api/code-review/{reviewId}/detail
export async function fetchCodeReviewDetail(reviewId) {
    try {
        const response = await authInstance.get(`/code-review/${reviewId}/detail`);
        const reviewDay = response.data.reviewDate.split('T')[0].replace(/-/g, '/');
        const reviewTime = response.data.reviewDate.split('T')[1].slice(0, 5);
        response.data.reviewDate = `${reviewDay} ${reviewTime}`;
        // response.data['filesChanged'] = response.data.commitCounts;
        console.log('가져온 코드 리뷰 상세 정보:', response.data);
        return response.data;
    }
    catch (error) {
        console.error('코드 리뷰 상세 정보 조회 실패:', error);
        throw error;
    }
}