// src/apis/profile.js
// 유저 프로필 관련 API

// profile dummy data
export const dummyProfile = {
    profileImage: null,       // 향후 URL로 교체
    nickname: 'CodeMaster',
    email: 'code@example.com',
    githubAccount: 'hiabc',
};

// 프로필 정보를 가져오는 함수
// 실제 api 사용시 아래 주석 처리된 코드 사용하기
// return axios.get('/api/profile').then(res => res.data);

export async function fetchProfile() {
    return Promise.resolve(dummyProfile);
}

// 프로필 정보를 서버에 업데이트하는 함수
// 실제 api 사용시 아래 주석 처리된 코드 사용하기
//   return axios.put('/api/profile', newProfile).then(res => res.data);
export async function updateProfile(newProfile) {
    return Promise.resolve(newProfile);
}