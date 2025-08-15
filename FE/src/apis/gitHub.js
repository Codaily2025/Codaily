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

// 깃허브 연동 팝업 처리 함수
export const handleGithubConnectPopup = (GITHUB_CLIENT_ID, onSuccess) => {
    // localStorage에서 JWT 토큰 가져오기
    const token = localStorage.getItem('authToken') || '';
    console.log('유저의 jwt token: ', token);

    // const githubAuthUrl = `https://github.com/login/oauth/authorize?client_id=${GITHUB_CLIENT_ID}&scope=repo,user&redirect_uri=http://localhost:8081/oauth/github/callback`;
    const githubAuthUrl = `https://github.com/login/oauth/authorize?client_id=${GITHUB_CLIENT_ID}&scope=repo,user,admin:repo_hook&redirect_uri=${import.meta.env.VITE_GITHUB_REDIRECT_URI}`;
    const popup = window.open(githubAuthUrl, 'github-auth', 'width=500,height=600');
    if (!popup) {
        alert('깃허브 연동 페이지를 열 수 없습니다. 브라우저 설정을 확인해주세요.');
        return null;
    }

    const handleMessage = (event) => {
        if (event.origin !== import.meta.env.VITE_BASE_URL) return;
        if (event.data.type === 'GITHUB_CONNECTED') {
            onSuccess();
            popup.close();
            window.removeEventListener('message', handleMessage);
        }
    };

    // 클린업: 팝업이 수동으로 닫힐 경우를 대비
    const checkClosed = setInterval(() => {
        if (popup.closed) {
            clearInterval(checkClosed);
            window.removeEventListener('message', handleMessage);
        }
    }, 1000);

    window.addEventListener('message', handleMessage);
    
    return { popup, handleMessage, checkClosed };
};

// 작성자: yeongenn
// 기존 레포지토리 연동 api
export const linkGithubRepository = async (projectId, repoName) => {
    try {
        const url = `repo/link?projectId=${projectId}&repoName=${repoName}`
        console.log('기존 레포지토리 연동 api url: ', url) //  작성자 : 오서로
        const response = await githubInstance.post(url)
        console.log('기존 레포지토리 연동 완료: ', response.data)
        return response.data
    } catch (error) {
        console.error('linkGithubRepository Error: ', error)
        throw new Error(error.response?.data?.message || '기존 레포지토리 연동에 실패했습니다.')
    }
}

// 작성자: yeongenn
// 새로운 레포지토리 생성 api
export const createGithubRepository = async (projectId, repoName) => {
    try {
        const url = `repo/create?projectId=${projectId}&repoName=${repoName}`
        console.log('새로운 레포지토리 연동 api url: ', url) //  작성자 : 오서로
        const response = await githubInstance.post(url)
        console.log('새로운 레포지토리 생성 완료: ', response.data)
        return response.data
    } catch (error) {
        console.error('linkGithubRepository Error: ', error)
        throw new Error(error.response?.data?.message || '새로운 레포지토리 생성에 실패했습니다.')
    }
}

// 사용자 GitHub 레포지토리 목록 조회 API
export const fetchGithubRepositories = async () => {
    try {
        const response = await githubInstance.get('repos');
        console.log('GitHub 레포지토리 목록 조회 완료: ', response.data);
        return response.data;
    } catch (error) {
        console.error('GitHub 레포지토리 목록 조회 실패: ', error);
        throw new Error(error.response?.data?.message || 'GitHub 레포지토리 목록 조회에 실패했습니다.');
    }
};

// 레포지토리 삭제 API
// /api/repos/{repoId}
export const deleteGithubRepository = async (repoId) => {
    try {
        const response = await authInstance.delete(`repos/${repoId}`);
        console.log('GitHub 레포지토리 삭제 완료: ', response.data);
        return response.data;
    } catch (error) {
        console.error('GitHub 레포지토리 삭제 실패: ', error);
        throw new Error(error.response?.data?.message || 'GitHub 레포지토리 삭제에 실패했습니다.');
    }
};