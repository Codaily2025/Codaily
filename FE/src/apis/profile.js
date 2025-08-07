// src/apis/profile.js
// 유저 프로필 관련 API
import { authInstance } from './axios';

// profile dummy data (fallback용)
export const dummyProfile = {
    profileImage: null,       // 향후 URL로 교체
    nickname: 'TempNickname',
    email: 'code@example.com',
    githubAccount: 'hiabc',
};

// 닉네임 조회 API
export async function fetchNickname(userId) {
    try {
        const response = await authInstance.get(`/users/${userId}/nickname`);
        console.log('fetchNickname response:', response.data);
        return response.data;
    } catch (error) {
        console.error('닉네임 조회 실패:', error);
        throw error;
    }
}

// 닉네임 수정 API
export async function updateNickname(userId, nickname) {
    console.log('updateNickname userId:', userId, 'nickname:', nickname);
    try {
        const response = await authInstance.patch(`/users/${userId}/nickname`, {
            nickname: nickname,
        }, {
            headers: {
                'Content-Type': 'application/json',
                // 'Authorization': `Bearer ${token}`
            }
        });
        console.log('updateNickname response:', response.data);
        return response.data;
    } catch (error) {
        console.error('닉네임 수정 실패:', error);
        throw error;
    }
}

// 프로필 정보를 가져오는 함수
// 실제 api 사용시 아래 주석 처리된 코드 사용하기
// return axios.get('/api/profile').then(res => res.data);

export async function fetchProfile() {
    // 임시로 userId 1을 사용 (실제로는 로그인된 사용자의 ID를 사용해야 함)
    try {
        const nicknameData = await fetchNickname(1);
        console.log('fetchProfile nicknameData:', nicknameData);
        return {
            profileImage: null,
            nickname: nicknameData.additionalProp1 || 'TempNickname', // API 응답에서 닉네임 추출
            email: 'code@example.com',
            githubAccount: 'hiabc',
        };
    } catch (error) {
        console.error('프로필 조회 실패, 더미 데이터 사용:', error);
        return dummyProfile;
    }
}

// 프로필 정보를 서버에 업데이트하는 함수
// 실제 api 사용시 아래 주석 처리된 코드 사용하기
//   return axios.put('/api/profile', newProfile).then(res => res.data);
export async function updateProfile(newProfile) {
    return Promise.resolve(newProfile);
}