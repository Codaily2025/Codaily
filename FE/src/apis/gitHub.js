// src/apis/gitHub.js
// 마이페이지 깃허브 관련 API
import { authInstance, githubInstance } from './axios';

// 깃허브 아이디 조회 API
// /api/github-account
export async function fetchGithubId() {
    try {
        const response = await authInstance.get(`github-account`);
        // 예상 응답 형식
        // {
        //     "githubId": "hiabc"
        // }
        return response.data;
    } catch (error) {
        console.error('깃허브 아이디 조회 실패:', error);
        throw error;
    }
}

// 깃허브 연동 해제 API
// oauth/github/unlink
export const disconnectGithub = async () => {
    try {
        const response = await githubInstance.delete('unlink');
        console.log('깃허브 연동 해제 응답:', response.data);
    } catch (error) {
        console.error('깃허브 연동 해제 실패:', error);
    }
};