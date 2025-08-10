import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getUserProjects, getLastWorkedProjectId, getKanbanTabFields, getFeatureItemsByKanbanTab, updateFeatureItemStatus } from '../apis/projectApi'

// Query Keys
export const PROJECT_QUERY_KEYS = {
    all: ['projects'],
    lists: () => [...PROJECT_QUERY_KEYS.all, 'list'],
    list: (filters) => [...PROJECT_QUERY_KEYS.lists(), { filters }],
    details: () => [...PROJECT_QUERY_KEYS.all, 'detail'],
    detail: (id) => [...PROJECT_QUERY_KEYS.details(), id],
    lastWorked: () => [...PROJECT_QUERY_KEYS.all, 'lastWorked'],
    kanbanTabs: () => [...PROJECT_QUERY_KEYS.all, 'kanbanTabs'],
    kanbanTab: (projectId) => [...PROJECT_QUERY_KEYS.kanbanTabs(), projectId],
    byField: (projectId, field) => [...PROJECT_QUERY_KEYS.kanbanTab(projectId), field]
}

// staleTime은 1시간으로 설정 (60분 * 60초 * 1000ms)
const STALE_TIME = 60 * 60 * 1000

// 사용자 프로젝트 목록 조회 (사이드바용)
export const useUserProjects = () => {
    return useQuery({
        queryKey: PROJECT_QUERY_KEYS.lists(),
        queryFn: getUserProjects,
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2, // 2시간 캐시
        retry: 2,
        refetchOnWindowFocus: false,
        onError: (error) => {
            console.error('useUserProjects Error:', error)
        }
    })
}

// 칸반 보드 탭용 필드 리스트 조회
export const useKanbanTabFields = (projectId) => {
    return useQuery({
        queryKey: PROJECT_QUERY_KEYS.kanbanTab(projectId),
        queryFn: () => getKanbanTabFields(projectId),
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2,
        retry: 2,
        refetchOnWindowFocus: false,
        enabled: !!projectId && projectId !== '', // projectId와 field가 모두 있고 빈 문자열이 아닐 때만 실행
        onError: (error) => {
            console.error('useKanbanTabFields Error:', error)
        }
    })
}

// 마지막으로 작업한 프로젝트 ID 조회
export const useLastWorkedProjectId = () => {
    return useQuery({
        queryKey: PROJECT_QUERY_KEYS.lastWorked(),
        queryFn: getLastWorkedProjectId,
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2,
        retry: 2,
        refetchOnWindowFocus: false,
        onError: (error) => {
            console.error('useLastWorkedProjectId Error:', error)
        }
    })
}

// 칸반 탭 필드별 기능 리스트 조회
export const useFeaturesByField = (projectId, field) => {
    return useQuery({
        queryKey: PROJECT_QUERY_KEYS.byField(projectId, field),
        queryFn: () => getFeatureItemsByKanbanTab(projectId, field),
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2, // 2시간 캐시
        retry: 2,
        refetchOnWindowFocus: false,
        enabled: !!projectId && !!field && field !== '', // projectId와 field가 모두 있고 빈 문자열이 아닐 때만 실행
        onError: (error) => {
            console.error('useFeaturesByField Error:', error)
        }
    })
}

// Feature Item 상태 변경 Mutation
export const useUpdateFeatureItemStatus = () => {
    const queryClient = useQueryClient()
    
    return useMutation({
        mutationFn: ({ projectId, featureId, newStatus }) => 
            updateFeatureItemStatus(projectId, featureId, newStatus),
        onSuccess: (data, variables) => {
            // 현재 필드의 캐시만 무효화
            if (variables.currentField) {
                queryClient.invalidateQueries({
                    queryKey: PROJECT_QUERY_KEYS.byField(variables.projectId, variables.currentField)
                })
            } else {
                // fallback: 모든 필드 데이터 무효화
                queryClient.invalidateQueries({
                    queryKey: PROJECT_QUERY_KEYS.byField(variables.projectId)
                })
            }
        },
        onError: (error) => {
            console.error('useUpdateFeatureItemStatus Error:', error)
        }
    })
}

// 특정 프로젝트 정보 조회
// export const useProjectDetail = (projectId, options = {}) => {
//     return useQuery({
//         queryKey: PROJECT_QUERY_KEYS.detail(projectId),
//         queryFn: () => getProjectById(projectId),
//         staleTime: STALE_TIME,
//         cacheTime: STALE_TIME * 2,
//         retry: 2,
//         refetchOnWindowFocus: false,
//         enabled: !!projectId, // projectId가 있을 때만 실행
//         onError: (error) => {
//             console.error('useProjectDetail Error:', error)
//         },
//         ...options
//     })
// }

