// src/queries/useProfile.js
// React Query 훅 정의
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchProfile, updateProfile, fetchNickname, updateNickname, fetchTechStack } from '../apis/profile';

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
// 캐시 키 : ['nickname', userId]
export function useNicknameQuery(userId) {
  return useQuery({
    queryKey: ['nickname', userId],
    queryFn: () => fetchNickname(userId),
    enabled: !!userId, // userId가 있을 때만 실행
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
    mutationFn: ({ userId, nickname }) => updateNickname(userId, nickname),
    onSuccess: (updated, variables) => {
      // 프로필 캐시 업데이트
      queryClient.setQueryData(['profile'], (oldData) => ({
        ...oldData,
        nickname: variables.nickname
      }));
      // 닉네임 캐시 업데이트
      queryClient.setQueryData(['nickname', variables.userId], { nickname: variables.nickname });
    },
  });
}