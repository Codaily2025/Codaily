// src/apis/user.js
import { authInstance } from './axios';

// 사용자 정보 조회 API
// /api/auth/me
export const fetchUserProfile = async () => {
  try {
    const response = await authInstance.get('auth/me');
    // console.log('user.js 사용자 정보 조회 응답:', response.data);
    return response.data;
    // 응답 예시
    // {
    //   authenticated: true,
    //   email: "구글계정닉네임",
    //   nickname: "구글계정닉네임",
    //   provider: "google",
    // }
  } catch (error) {
    console.error('사용자 정보를 가져오는데 실패했습니다.', error);
    throw error;
  }
};

// 회원 탈퇴 API
// /api/user
export const deleteUser = async () => {
  try {
    const response = await authInstance.delete('user');
    console.log('회원 탈퇴 응답:', response.data);
    return response.data;
  } catch (error) {
    console.error('회원 탈퇴 실패:', error);
    throw error;
  }
};