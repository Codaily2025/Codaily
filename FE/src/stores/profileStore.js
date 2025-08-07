// src/stores/profileStore.js
import { create } from 'zustand'
import { dummyProfile, fetchNickname, updateNickname } from '../apis/profile' // 더미 데이터와 API 함수

export const useProfileStore = create((set, get) => ({
  profile: dummyProfile,
  isLoading: false,
  error: null,
  
  // profile: {
  //   profileImage: null,
  //   nickname: '',
  //   email: '',
  //   githubAccount: '',
  // },
  
  // 전체 프로필 세팅
  setProfile: (newProfile) => set({ profile: newProfile }),
  
  // 필드 단위 업데이트
  updateField: (key, value) =>
    set((state) => ({
      profile: { ...state.profile, [key]: value },
    })),
    
  // 닉네임 조회 (API 호출)
  fetchNickname: async (userId) => {
    set({ isLoading: true, error: null });
    try {
      const nicknameData = await fetchNickname(userId);
      set((state) => ({
        profile: { 
          ...state.profile, 
          nickname: nicknameData.additionalProp1 || 'TempNickname' 
        },
        isLoading: false
      }));
      return nicknameData;
    } catch (error) {
      set({ 
        error: error.message || '닉네임 조회에 실패했습니다.',
        isLoading: false 
      });
      throw error;
    }
  },

  // 닉네임 수정 (API 호출)
  updateNickname: async (userId, nickname) => {
    set({ isLoading: true, error: null });
    try {
      const response = await updateNickname(userId, nickname);
      set((state) => ({
        profile: { 
          ...state.profile, 
          nickname: nickname 
        },
        isLoading: false
      }));
      return response;
    } catch (error) {
      set({ 
        error: error.message || '닉네임 수정에 실패했습니다.',
        isLoading: false 
      });
      throw error;
    }
  },
  
  // 로딩 상태 초기화
  clearLoading: () => set({ isLoading: false, error: null }),
}))
