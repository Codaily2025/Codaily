// src/queries/useProfile.js
// React Query 훅 정의
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchProfile, updateProfile } from '../apis/profile';

// 프로필 조회용 커스텀 훅
// 캐시 키 : ['profile']
export function useProfileQuery() {
  return useQuery({
    queryKey: ['profile'],
    queryFn: fetchProfile,
    // placeholderData: dummyProfile, // 필요 시 더미 데이터 사용
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