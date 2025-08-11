import { useQuery, useMutation } from '@tanstack/react-query';
import { fetchUserProfile, deleteUser } from '../apis/user';

// 사용자 정보 조회용 커스텀 훅
// 캐시 키 : ['user']
export const useUserQuery = () => {
    return useQuery({
        queryKey: ['user'],
        queryFn: fetchUserProfile,
        initialData: { authenticated: false },
        placeholderData: (prev) => prev ?? { authenticated: false },
    });
};

// 회원 탈퇴용 커스텀 훅
// 캐시 키 : ['user']
export const useDeleteUserMutation = () => {
    return useMutation({
        mutationFn: deleteUser,
        onError: (error) => {
            console.error('회원 탈퇴 실패:', error);
        },
    });
};