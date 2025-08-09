// src/queries/useProjectsQuery.js
import { useQuery } from '@tanstack/react-query';
import { fetchProjectsByUserId } from '../apis/mypageProject'; // API 함수를 불러옵니다

export const useProjectsQuery = (userId) => {
  return useQuery({
    queryKey: ['projects', userId],       // 쿼리를 위한 고유 키
    queryFn: () => fetchProjectsByUserId(userId),       // 데이터를 가져오는 함수
    enabled: !!userId, // userId가 있을 때만 쿼리 실행
    staleTime: 1000 * 60 * 5,     // 데이터를 5분 동안 신선한 상태로 간주
    retry: 1, // 재시도 횟수 제한
    retryDelay: 1000, // 재시도 간격
  });
};
