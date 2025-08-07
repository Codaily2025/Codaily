// src/stores/profileStore.js
import { create } from 'zustand'
import { dummyProfile } from '../apis/profile' // 더미 데이터

export const useProfileStore = create((set) => ({
  profile: dummyProfile,
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
}))
