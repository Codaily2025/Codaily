// src/apis/profile.js
// 유저 프로필 관련 API
import { authInstance } from './axios';
import { useAuthStore } from '../stores/authStore';

// const { user } = useAuthStore();
// const userId = user?.id;
const userId = 1;

// profile dummy data (fallback용)
export const dummyProfile = {
    userId: userId,               // 임시 userId
    profileImage: null,       // 향후 URL로 교체
    nickname: 'TempNickname',
    email: 'code@example.com',
    githubAccount: 'hiabc',
};

// 닉네임 조회 API
export async function fetchNickname() {
    try {
        const response = await authInstance.get(`nickname`);
        console.log('api에서 가져온 닉네임 데이터:', response.data);
        return response.data;
    } catch (error) {
        console.error('닉네임 조회 실패:', error);
        throw error;
    }
}

// 닉네임 수정 API
export async function updateNickname(nickname) {
    // console.log('nickname:', nickname);
    try {
        const response = await authInstance.patch(`nickname`, {
            nickname: nickname,
        }, {
            headers: {
                'Content-Type': 'application/json',
                // 'Authorization': `Bearer ${token}`
            }
        });
        console.log('닉네임 업데이트 응답:', response);
        return { nickname: nickname };
    } catch (error) {
        console.error('닉네임 수정 실패:', error);
        throw error;
    }
}

// 기술스택 조회 API
export async function fetchTechStack(userId) {
    try {
        const response = await authInstance.get(`/github/tech-stack`);
        console.log('프로필 기술 스택 조회 응답:', response.data);
        return response.data;
    } catch (error) {
        console.error('기술스택 조회 실패:', error);
        throw error;
    }
}

// 프로필 정보를 가져오는 함수
export async function fetchProfile() {
    // 임시로 userId 1을 사용 (실제로는 로그인된 사용자의 ID를 사용해야 함)
    const userId = 1; // 실제로는 로그인된 사용자의 ID를 가져와야 함
    try {
        const nicknameData = await fetchNickname(userId);
        // console.log('fetchProfile nicknameData:', nicknameData);
        // {nickname: 'TempNickname111'}
        const techStackData = await fetchTechStack(userId); // 객체
        return {
            userId: userId, // userId 추가
            profileImage: null,
            nickname: nicknameData.nickname || 'TempNickname', // API 응답에서 닉네임 추출
            email: 'code@example.com',
            githubAccount: 'hiabc',
            techStack: techStackData.technologies,
        };
    } catch (error) {
        console.error('프로필 조회 실패, 더미 데이터 사용:', error);
        return {
            ...dummyProfile,
            userId: userId, // 더미 데이터에도 userId 추가
        };
    }
}

// 프로필 이미지 업로드 API
// api/upload-profile-image
// request body: { file: string}
export async function uploadProfileImage(file) { 
    try {
        const formData = new FormData(); // 파일 업로드 시 필요
        formData.append('file', file);
        const response = await authInstance.post(`upload-profile-image`, formData);
        console.log('프로필 이미지 업로드 응답:', response.data);
        return response.data;
        // 예상 응답 형식
        // {
        //     "message": "프로필 이미지가 업로드되었습니다.",
        //     "imageUrl": "string"
        // }
    } catch (error) {
        console.error('프로필 이미지 업로드 실패:', error);
        throw error;
    }
}

// 프로필 이미지 삭제 API
// /api/profile-image
export async function deleteProfileImage() {
    try {
        const response = await authInstance.delete(`profile-image`);
        console.log('프로필 이미지 삭제 응답:', response.data);
        return response.data;
    } catch (error) {
        console.error('프로필 이미지 삭제 실패:', error);
        throw error;
    }
}

// 프로필 정보를 서버에 업데이트하는 함수
// 실제 api 사용시 아래 주석 처리된 코드 사용하기
//   return axios.put('/api/profile', newProfile).then(res => res.data);
export async function updateProfile(newProfile) {
    return Promise.resolve(newProfile);
}