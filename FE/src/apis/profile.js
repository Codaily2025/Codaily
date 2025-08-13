// src/apis/profile.js
// 유저 프로필 관련 API
import { authInstance } from "./axios";
import { useAuthStore } from "../stores/authStore";
// import { useUserQuery } from '../queries/useUser';
import { fetchGithubId } from "./gitHub";

// const { data: user } = useUserQuery(); // 현재 api/auth/me에서는 userId를 반환하지 않음
// const userId = user?.id;
const userId = 1;

// 프로필 더미데이터 (fallback용)
export const dummyProfile = {
    userId: userId, // 임시 userId
    profileImage: null, // 향후 URL로 교체
    nickname: "TempNickname",
    email: "code@example.com",
    githubAccount: "hiabc",
};

// 닉네임 조회 API
export async function fetchNickname() {
    try {
        const response = await authInstance.get(`nickname`);
        console.log("api에서 가져온 닉네임 데이터:", response.data);
        return response.data;
    } catch (error) {
        console.error("닉네임 조회 실패:", error);
        throw error;
    }
}

// 닉네임 수정 API
export async function updateNickname(nickname) {
    // console.log('nickname:', nickname);
    try {
        const response = await authInstance.patch(
            `nickname`,
            {
                nickname: nickname,
            },
            {
                headers: {
                    "Content-Type": "application/json",
                    // 'Authorization': `Bearer ${token}`
                },
            }
        );
        console.log("닉네임 업데이트 응답:", response);
        return { nickname: nickname };
    } catch (error) {
        console.error("닉네임 수정 실패:", error);
        throw error;
    }
}

// 기술스택 조회 API
export async function fetchTechStack() {
    try {
        const response = await authInstance.get(`/github/tech-stack`);
        console.log("프로필 기술 스택 조회 응답:", response.data);
        return response.data;
    } catch (error) {
        console.error("기술스택 조회 실패:", error);
        throw error;
    }
}

// GitHub 기술스택 동기화 API
// /api/github/tech-stack/sync
export async function syncGithubTechStack() {
    try {
        const response = await authInstance.post(`github/tech-stack/sync`);
        console.log("GitHub 기술스택 동기화 응답:", response.data);
        return response.data;
    } catch (error) {
        console.error("GitHub 기술스택 동기화 실패:", error);
        throw error;
    }
}

// 프로필 이미지 업로드 API
// api/upload-profile-image
// request body: { file: string}
export async function uploadProfileImage(file) {
    try {
        const formData = new FormData(); // 파일 업로드 시 필요
        formData.append("file", file);
        const response = await authInstance.post(
            `upload-profile-image`,
            formData
        );
        // console.log('프로필 이미지 업로드 응답:', response.data);
        return response.data;
        // 예상 응답 형식
        // {
        //     "message": "프로필 이미지가 업로드되었습니다.",
        //     "imageUrl": "string"
        // }
    } catch (error) {
        console.error("프로필 이미지 업로드 실패:", error);
        throw error;
    }
}
// 프로필 이미지 조회 API
// /api/profile-image
export async function getProfileImage() {
    try {
        const response = await authInstance.get(`profile-image`);
        // console.log('프로필 이미지 조회 응답:', response.data);
        return response.data;
        // 예상 응답 형식
        // {
        //     "imageUrl": "string"
        // }
    } catch (error) {
        console.error("프로필 이미지 조회 실패:", error);
        throw error;
    }
}

// 프로필 정보를 가져오는 함수
export async function fetchProfile() {
    // 임시로 userId 1을 사용 (실제로는 로그인된 사용자의 ID를 사용해야 함)
    const userId = 1; // 실제로는 로그인된 사용자의 ID를 가져와야 함
    try {
        const nicknameData = await fetchNickname();
        const githubId = await fetchGithubId();
        const profileImage = await getProfileImage();
        // console.log('fetchProfile nicknameData:', nicknameData);
        // {nickname: 'TempNickname111'}
        const techStackData = await fetchTechStack(); // 객체
        return {
            userId: userId, // userId 추가
            profileImage: profileImage?.imageUrl || null,
            nickname: nicknameData.nickname || "TempNickname", // API 응답에서 닉네임 추출
            email: "code@example.com",
            githubAccount: githubId?.githubId || "연동 안됨",
            techStack: techStackData.technologies,
        };
    } catch (error) {
        console.error("프로필 조회 실패, 더미 데이터 사용:", error);
        return {
            ...dummyProfile,
            userId: userId, // 더미 데이터에도 userId 추가
        };
    }
}

// 프로필 이미지 삭제 API
// /api/profile-image
export async function deleteProfileImage() {
    try {
        const response = await authInstance.delete(`profile-image`);
        console.log("프로필 이미지 삭제 응답:", response.data);
        return response.data;
    } catch (error) {
        console.error("프로필 이미지 삭제 실패:", error);
        throw error;
    }
}

// 프로필 정보를 서버에 업데이트하는 함수
export async function updateProfile(newProfile) {
    return Promise.resolve(newProfile);
}

// 회원 탈퇴 API (소셜 로그인 전용)
// DELETE /api/user
export async function deleteAccount() {
    try {
        const res = await authInstance.delete(`user`);
        console.log("회원 탈퇴 성공:", res.data);

        // 클라이언트 상태/토큰 정리 (Pinia/Zustand 등)
        try {
            const { logout } = useAuthStore.getState?.() || {};
            if (typeof logout === "function") logout();
            // 필요 시 localStorage/sessionStorage도 정리
            // localStorage.removeItem('accessToken');
            // sessionStorage.clear();
        } catch (e) {
            console.warn("로컬 로그아웃 처리 중 경고:", e);
        }

        return res.data; // 예: { message: '탈퇴 완료' }
    } catch (error) {
        if (error?.response) {
            const { status, data } = error.response;
            console.error(`회원 탈퇴 실패(${status}):`, data);
        } else {
            console.error("네트워크/알 수 없는 오류:", error);
        }
        throw error;
    }
}
