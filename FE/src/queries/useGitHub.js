// src/queries/usegitHub.js
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchGithubId, disconnectGithub, linkGithubRepository, createGithubRepository, fetchGithubRepositories, deleteGithubRepository } from '../apis/gitHub';
import { syncGithubTechStack } from '../apis/profile';
import { useGitHubStore } from '../stores/gitHubStore';

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

// 깃허브 기술스택 동기화 뮤테이션
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
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ projectId, repoName }) => linkGithubRepository(projectId, repoName),
        onSuccess: (data, variables) => {
            console.log('useLinkGithubRepoMutation 실행: ', variables)
            // 레포지토리 목록 캐시 무효화
            queryClient.invalidateQueries({ queryKey: ['githubRepositories'] });
            if (onSuccessCallback) {
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

// GitHub 레포지토리 목록 조회 쿼리
export const useGithubRepositoriesQuery = () => {
    return useQuery({
        queryKey: ['githubRepositories'],
        queryFn: fetchGithubRepositories,
        staleTime: 5 * 60 * 1000, // 5분
        cacheTime: 10 * 60 * 1000, // 10분
    });
};

// 새로운 레포지토리 생성 뮤테이션 (프로젝트 설정 모달용)
export const useCreateNewGithubRepoMutation = (onSuccessCallback) => {
    const queryClient = useQueryClient();
    
    return useMutation({
        mutationFn: ({ projectId, repoName }) => createGithubRepository(projectId, repoName),
        onSuccess: (data, variables) => {
            console.log('새로운 GitHub 레포지토리 생성 성공: ', variables);
            // 레포지토리 목록 캐시 무효화
            queryClient.invalidateQueries({ queryKey: ['githubRepositories'] });
            if (onSuccessCallback) {
                onSuccessCallback(variables.repoName);
            }
        },
        onError: (error) => {
            console.error('새로운 GitHub 레포지토리 생성 실패: ', error);
        }
    });
};

// 레포지토리 삭제 뮤테이션
export const useDeleteGithubRepoMutation = (onSuccessCallback, onErrorCallback) => {
    const queryClient = useQueryClient();
    const { removeRepository } = useGitHubStore();

    return useMutation({
        mutationFn: ({ repoId }) => deleteGithubRepository(repoId),
        onMutate: async ({ repoId }) => {
            // 낙관적 업데이트: UI에서 즉시 제거
            removeRepository(repoId);
            
            // 기존 데이터 백업
            const previousRepos = queryClient.getQueryData(['githubRepositories']);
            
            return { previousRepos };
        },
        onSuccess: (data, variables) => {
            console.log('레포지토리 삭제 성공:', variables.repoId);
            // 캐시 무효화
            queryClient.invalidateQueries({ queryKey: ['githubRepositories'] });
            if (onSuccessCallback) {
                onSuccessCallback(variables.repoId);
            }
        },
        onError: (error, variables, context) => {
            console.error('레포지토리 삭제 실패:', error);
            // 에러 시 롤백
            if (context?.previousRepos) {
                queryClient.setQueryData(['githubRepositories'], context.previousRepos);
            }
            if (onErrorCallback) {
                onErrorCallback(error);
            }
        }
    });
};


