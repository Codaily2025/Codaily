import { useQuery } from '@tanstack/react-query'
import { getUserProjects, getLastWorkedProjectId, getProjectById } from '../apis/projectApi'

// Query Keys
export const PROJECT_QUERY_KEYS = {
    all: ['projects'],
    lists: () => [...PROJECT_QUERY_KEYS.all, 'list'],
    list: (filters) => [...PROJECT_QUERY_KEYS.lists(), { filters }],
    details: () => [...PROJECT_QUERY_KEYS.all, 'detail'],
    detail: (id) => [...PROJECT_QUERY_KEYS.details(), id],
    lastWorked: () => [...PROJECT_QUERY_KEYS.all, 'lastWorked']
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

// 특정 프로젝트 상세 정보 조회
export const useProjectDetail = (projectId, options = {}) => {
    return useQuery({
        queryKey: PROJECT_QUERY_KEYS.detail(projectId),
        queryFn: () => getProjectById(projectId),
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2,
        retry: 2,
        refetchOnWindowFocus: false,
        enabled: !!projectId, // projectId가 있을 때만 실행
        onError: (error) => {
            console.error('useProjectDetail Error:', error)
        },
        ...options
    })
}

// 마지막으로 작업한 프로젝트의 상세 정보를 가져오는 커스텀 훅
export const useLastWorkedProject = () => {
    // 1단계: 마지막 작업 프로젝트 ID 가져오기
    const { 
        data: lastWorkedProjectId, 
        isLoading: isLoadingId, 
        error: idError 
    } = useLastWorkedProjectId()

    // 2단계: 해당 프로젝트 상세 정보 가져오기
    const { 
        data: projectDetail, 
        isLoading: isLoadingDetail, 
        error: detailError 
    } = useProjectDetail(lastWorkedProjectId, {
        enabled: !!lastWorkedProjectId // ID가 있을 때만 실행
    })

    return {
        data: projectDetail,
        isLoading: isLoadingId || isLoadingDetail,
        error: idError || detailError,
        lastWorkedProjectId
    }
}