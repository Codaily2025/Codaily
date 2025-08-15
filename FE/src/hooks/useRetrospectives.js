import { useInfiniteQuery } from '@tanstack/react-query'
import { getAllRetrospectives, getProjectRetrospectives } from '@/apis/projectApi'

// 더미 데이터 import (실제 서버 연동 전까지 사용)
import { fetchedData, fetchedData2, fetchedData3, fetchedData4 } from '/retrospectiveRes.js'

const STALE_TIME = 60 * 60 * 1000 // 1시간

// 오늘 날짜를 yyyy-MM-dd 형식으로 반환하는 함수
const getTodayString = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 더미 데이터 시뮬레이션 함수
const getDummyData = (pageParam) => {
  // 오늘 날짜나 초기 페이지 파라미터일 때는 첫 번째 더미 데이터 반환
  if (pageParam === getTodayString() || pageParam === null || pageParam === undefined) {
    return fetchedData
  }
  
  switch (pageParam) {
    case '2025-07-31':
      return fetchedData2
    case '2025-07-16':
      return fetchedData3
    case '2025-07-01':
      return fetchedData4
    default:
      return { items: [], hasNext: false, nextBefore: null }
  }
}

export const RETROSPECTIVE_QUERY_KEYS = {
  all: ['retrospectives'],
  allProjects: () => [...RETROSPECTIVE_QUERY_KEYS.all, 'allProjects'],
  project: (projectId) => [...RETROSPECTIVE_QUERY_KEYS.all, 'project', projectId],
}

// 모든 프로젝트 회고 데이터 무한 스크롤
export const useAllRetrospectives = (useDummyData = true) => {
  return useInfiniteQuery({
    queryKey: RETROSPECTIVE_QUERY_KEYS.allProjects(),
    queryFn: async ({ pageParam }) => {
      if (useDummyData) {
        // 더미 데이터 사용
        return getDummyData(pageParam)
      } else {
        // 실제 API 호출 - pageParam은 'yyyy-MM-dd' 형식의 날짜
        const response = await getAllRetrospectives(pageParam, 15)
        return response
      }
    },
    // 첫 페이지 로드 시 오늘 날짜를 before 파라미터로 전달
    initialPageParam: getTodayString(),
    getNextPageParam: (lastPage) => {
      return lastPage?.hasNext ? lastPage.nextBefore : undefined
    },
    staleTime: STALE_TIME,
    cacheTime: STALE_TIME * 2,
    retry: 2,
    refetchOnWindowFocus: false,
    onError: (error) => {
      console.error('useAllRetrospectives Error:', error)
    }
  })
}

// 특정 프로젝트 회고 데이터 무한 스크롤
export const useProjectRetrospectives = (projectId, useDummyData = true) => {
  return useInfiniteQuery({
    queryKey: RETROSPECTIVE_QUERY_KEYS.project(projectId),
    queryFn: async ({ pageParam }) => {
      if (useDummyData) {
        // 더미 데이터 사용 (프로젝트별 필터링)
        const dummyData = getDummyData(pageParam)
        const filteredItems = dummyData.items.filter(item => item.projectId === projectId)
        return {
          ...dummyData,
          items: filteredItems
        }
      } else {
        // 실제 API 호출 - pageParam은 'yyyy-MM-dd' 형식의 날짜
        const response = await getProjectRetrospectives(projectId, pageParam, 15)
        return response
      }
    },
    // 첫 페이지 로드 시 오늘 날짜를 before 파라미터로 전달
    initialPageParam: getTodayString(),
    getNextPageParam: (lastPage) => {
      return lastPage?.hasNext ? lastPage.nextBefore : undefined
    },
    enabled: !!projectId,
    staleTime: STALE_TIME,
    cacheTime: STALE_TIME * 2,
    retry: 2,
    refetchOnWindowFocus: false,
    onError: (error) => {
      console.error('useProjectRetrospectives Error:', error)
    }
  })
}