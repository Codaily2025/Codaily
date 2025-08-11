// src/stores/authStore.js
import { create } from 'zustand';

export const useAuthStore = create((set) => ({
// export const useAuthStore = create((set, get) => ({
  token: localStorage.getItem('authToken') || null,
  isAuthenticated: !!localStorage.getItem('authToken'),
  // user: JSON.parse(localStorage.getItem('user')) || null,
  
  setToken: (token) => {
    if (token) {
      localStorage.setItem('authToken', token);
      set({ token, isAuthenticated: true });
      console.log('토큰이 스토어와 로컬 스토리지에 저장됨');
    } else {
      console.warn('토큰을 설정하지 못하고, null 또는 빈 값을 설정하려고 함');
    }
  },
  
  logout: () => {
    localStorage.removeItem('authToken');
    // localStorage.removeItem('profile');
    // localStorage.removeItem('githubId');
    // localStorage.removeItem('techStack');
    // localStorage.removeItem('profileImage');

    // zustand 스토어 상태 초기화
    set({ token: null, isAuthenticated: false });
    console.log('로그아웃, 토큰 삭제');
  },
}));
