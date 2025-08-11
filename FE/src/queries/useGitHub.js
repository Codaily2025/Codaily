// src/queries/usegitHub.js
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchGithubId, disconnectGithub } from '../apis/gitHub';

export const useGithubIdQuery = () => {
    return useQuery({
        queryKey: ['githubId'],
        queryFn: fetchGithubId,
        initialData: { githubId: null },                  // 항상 같은 shape
        placeholderData: (prev) => prev ?? { githubId: null },
    });
};

export const useDisconnectGithubMutation = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: disconnectGithub,
        onMutate: async () => {
            await queryClient.cancelQueries({ queryKey: ['githubId'] });
            const prev = queryClient.getQueryData(['githubId']);
            // 버튼 라벨이 곧바로 "연동"으로 바뀌도록 캐시만 비워줌 (invalidateQueries는 onSuccess에서만 호출)
            queryClient.setQueryData(['githubId'], { githubId: null });
            return { prev };
        },
        // 실패 시 캐시 롤백
        onError: (_err, _vars, ctx) => {
            if (ctx?.prev !== undefined) {
                queryClient.setQueryData(['githubId'], ctx.prev);
            }
        },
        // 성공 시 캐시 무효화
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['githubId'] });
            queryClient.invalidateQueries({ queryKey: ['profile'] });
        },
    });
};
