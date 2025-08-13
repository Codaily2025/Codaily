// src/queries/useProfile.js
// React Query 훅 정의
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchProfile, updateProfile, fetchNickname, updateNickname, fetchTechStack, uploadProfileImage, getProfileImage, deleteProfileImage } from '../apis/profile';

// 프로필 조회용 커스텀 훅
// 캐시 키 : ['profile']
export function useProfileQuery() {
  return useQuery({
    queryKey: ['profile'],
    queryFn: fetchProfile,
    // placeholderData: dummyProfile, // 필요 시 더미 데이터 사용
  });
}

// 닉네임 조회용 커스텀 훅
// 캐시 키 : ['nickname']
export function useNicknameQuery() {
  return useQuery({
    queryKey: ['nickname'],
    queryFn: () => fetchNickname(),
    // enabled: !!userId, // userId가 있을 때만 실행
  });
}

// 기술스택 조회용 커스텀 훅
// 캐시 키 : ['techStack', userId]
export function useTechStackQuery(userId) {
  return useQuery({
    queryKey: ['techStack', userId],
    queryFn: () => fetchTechStack(userId),
    enabled: !!userId, // userId가 있을 때만 실행
  });
}

// 프로필 업데이트용 커스텀 훅
// onSuccess에서 캐시 자동 업데이트
export function useUpdateProfileMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateProfile,
    onSuccess: (updated) => {
      queryClient.setQueryData(['profile'], updated);
    },
  });
}

// 닉네임 업데이트용 커스텀 훅
export function useUpdateNicknameMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ nickname }) => updateNickname(nickname),
    onSuccess: (updated, variables) => {
      // 프로필 캐시 업데이트
      queryClient.setQueryData(['profile'], (oldData) => ({
        ...oldData,
        nickname: variables.nickname
      }));
      // 닉네임 캐시 업데이트
      queryClient.setQueryData(['nickname'], { nickname: variables.nickname });
    },
  });
}

// 프로필 이미지 업로드용 커스텀 훅
export function useUploadProfileImageMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: uploadProfileImage,
    onSuccess: (response) => {
      // 프로필 캐시 업데이트
      queryClient.setQueryData(['profile'], (oldData) => ({
        ...oldData,
        profileImage: response.imageUrl
      }));
    },
  });
}

// 프로필 이미지 조회용 커스텀 훅
export function useGetProfileImageQuery() {
  return useQuery({
    queryKey: ['profileImage'],
    queryFn: getProfileImage,
  });
}

// 프로필 이미지 삭제용 커스텀 훅
export function useDeleteProfileImageMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteProfileImage,
    onSuccess: () => {
      // 프로필 이미지 캐시 초기화
      queryClient.setQueryData(['profileImage'], { imageUrl: null });
      // 프로필 캐시에서도 이미지 URL 제거
      queryClient.setQueryData(['profile'], (oldData) => ({
        ...oldData,
        profileImage: null
      }));
    },
  });
}