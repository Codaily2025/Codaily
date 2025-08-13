// src/apis/mypageProductivityGraph.js
// 유저 생산성 관련 API
import { authInstance } from './axios';

const userId = 1;

// 생산성 그래프 조회 API
// 사용자의 모든 프로젝트 단위(주/월) 기준 생산성 데이터
// http://localhost:8081/api/users/analytics/productivity
export async function fetchProductivityGraph(period, startDate, endDate) {
    try {
        console.log('생산성 그래프 조회 파라미터:', period, startDate, endDate);
        const response = await authInstance.get(`users/analytics/productivity`, {
            params: {
                period: period,
                startDate: startDate,
                endDate: endDate
            }
        });
        console.log('생산성 그래프 조회 성공:', response.data);
        return response.data;
    } catch (error) {
        console.error('생산성 그래프 조회 실패:', error);
        throw error;
    }
}