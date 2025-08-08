// src/queries/useProjectMutation.js
// 프로젝트 설정 수정 위해 생성
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateProjectAPI } from '../stores/mypageProjectStore';

export const useUpdateProjectMutation = (userId) => {
  const queryClient = useQueryClient();

  return useMutation({
    // mutationFn: 서버에 변경 요청을 보낼 함수
    mutationFn: updateProjectAPI, 
    
    // onSuccess: mutation이 성공했을 때 실행할 콜백
    onSuccess: () => {
      console.log('Mutation successful, invalidating projects query...');
      // 프로젝트 목록 쿼리를 무효화하여 최신 데이터를 다시 가져오도록 합니다.
      // ['projects', userId]는 useProjectsQuery에서 사용한 queryKey와 일치해야 합니다.
      queryClient.invalidateQueries({ queryKey: ['projects', userId] });
    },

    // onError: mutation이 실패했을 때 실행할 콜백
    onError: (error) => {
        // console.log('projects', projects, 'userId', userId);
      console.error('Mutation failed:', error);
      // 여기에 사용자에게 에러를 알리는 로직을 추가할 수 있습니다. (e.g., toast 알림)
    },
  });
};