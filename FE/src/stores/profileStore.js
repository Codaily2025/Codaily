// src/stores/profileStore.js
// 클라이언트 상태만 관리하는 스토어 (프로필 편집 폼 상태, 임시 데이터 등)
import { create } from 'zustand'

export const useProfileStore = create((set, get) => ({
  // 프로필 편집 폼 데이터 (임시 상태)
  editFormData: {
    nickname: '',
    email: '',
    githubAccount: '',
    profileImage: null,
  },
  
  // 프로필 편집 관련 UI 상태
  previewImage: null,
  formErrors: {},
  isEmailVerifying: false,
  isEmailVerified: false,
  isGithubConnecting: false,
  isGithubReconnected: false,
  hasVerifiedEmailOnce: false,
  
  // 폼 데이터 초기화 (서버 데이터로부터)
  initializeFormData: (profileData) => set({
    editFormData: {
      nickname: profileData.nickname || '',
      email: profileData.email || '',
      githubAccount: profileData.githubAccount || '',
      profileImage: profileData.profileImage || null,
    },
    previewImage: profileData.profileImage || null,
    formErrors: {},
    isEmailVerified: true,
    isEmailVerifying: false,
    isGithubReconnected: false,
    isGithubConnecting: false,
    hasVerifiedEmailOnce: false,
  }),
  
  // 폼 필드 업데이트
  updateFormField: (key, value) =>
    set((state) => ({
      editFormData: { ...state.editFormData, [key]: value },
    })),
  // 프리뷰 이미지 업데이트
  setPreviewImage: (image) => set({ previewImage: image }),
  
  // 폼 에러 설정
  setFormErrors: (errors) => set({ formErrors: errors }),
  
  // 폼 에러 필드별 설정
  setFormError: (field, error) =>
    set((state) => ({
      formErrors: { ...state.formErrors, [field]: error },
    })),
    
  // 폼 에러 초기화
  clearFormErrors: () => set({ formErrors: {} }),
  
  // 이메일 인증 상태 관리
  setEmailVerifying: (isVerifying) => set({ isEmailVerifying: isVerifying }),
  setEmailVerified: (isVerified) => set({ isEmailVerified: isVerified }),
  setHasVerifiedEmailOnce: (hasVerified) => set({ hasVerifiedEmailOnce: hasVerified }),
  
  // GitHub 연동 상태 관리
  setGithubConnecting: (isConnecting) => set({ isGithubConnecting: isConnecting }),
  setGithubReconnected: (isReconnected) => set({ isGithubReconnected: isReconnected }),
  
  // 폼 상태 초기화
  resetFormState: () => set({
    editFormData: {
      nickname: '',
      email: '',
      githubAccount: '',
      profileImage: null,
    },
    previewImage: null,
    formErrors: {},
    isEmailVerifying: false,
    isEmailVerified: false,
    isGithubConnecting: false,
    isGithubReconnected: false,
    hasVerifiedEmailOnce: false,
  }),
}))
