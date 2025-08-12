// src/queries/usegitHub.js
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchGithubId, disconnectGithub, linkGithubRepository, createGithubRepository } from '../apis/gitHub';
import { syncGithubTechStack } from '../apis/profile';

// 깃허브 아이디 조회 커스텀 훅
// 캐시 키 : ['githubId']
export const useGithubIdQuery = () => {
    return useQuery({
        queryKey: ['githubId'],
        queryFn: fetchGithubId,
        initialData: { githubId: null },                  // 항상 같은 shape
        placeholderData: (prev) => prev ?? { githubId: null },
    });
};

// 깃허브 연동 해제 커스텀 훅
// 캐시 키 : ['githubId']
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

export function useGithubTechStackSyncMutation() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: syncGithubTechStack,
        onSuccess: (data) => {
            console.log('GitHub 기술스택 동기화 성공:', data);
            // 프로필 쿼리를 다시 가져와서 UI 업데이트
            queryClient.invalidateQueries({ queryKey: ['profile'] });
            queryClient.invalidateQueries({ queryKey: ['techStack'] });
        },
        onError: (error) => {
            console.error('GitHub 기술스택 동기화 실패:', error);
        }
    });
}

// 작성자: yeongenn
// 기존 레포지토리 연동 mutation
export const useLinkGithubRepoMutation = (onSuccessCallback) => {
    // const queryClient = useQueryClient()

    return useMutation({
        mutationFn: ({ projectId, repoName }) => linkGithubRepository(projectId, repoName),
        onSuccess: (data, variables) => {
            console.log('useLinkGithubRepoMutation 실행: ', variables)
            if (onSuccessCallback) {
                // console.log(onSuccessCallback)
                onSuccessCallback(variables.projectId)
            }
        },
        onError: (error) => {
            console.error('useLinkGithubRepoMutation Error: ', error)
        }
    })
}

// 작성자: yeongenn
// 새로운 레포지토리 연동 mutation
export const useCreateGithubRepoMutation = (onSuccessCallback) => {
    // const queryClient = useQueryClient()

    return useMutation({
        mutationFn: ({ projectId, repoName }) => createGithubRepository(projectId, repoName),
        onSuccess: (data, variables) => {
            console.log('useCreateGithubRepoMutation 실행: ', variables)
            if (onSuccessCallback) {
                // console.log(onSuccessCallback)
                onSuccessCallback(variables.projectId)
            }
        },
        onError: (error) => {
            console.error('useCreateGithubRepoMutation Error: ', error)
        }
    })
}
