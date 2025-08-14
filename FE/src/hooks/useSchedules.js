import { useQuery } from '@tanstack/react-query'
import { getUserSchedule, getProjectSchedule } from '../apis/scheduleApi'

// query keys
export const SCHEDULE_QUERY_KEYS = {
    all: ['schedules'],
    monthly: () => [...SCHEDULE_QUERY_KEYS.all, 'monthly'],
    byMonth: (year, month) => [...SCHEDULE_QUERY_KEYS.monthly(), { year, month }],
    project: () => [...SCHEDULE_QUERY_KEYS.all, 'project'],
    projectByMonth: (projectId, year, month) => [...SCHEDULE_QUERY_KEYS.project(), { projectId, year, month }],
}

// staleTime 설정
const STALE_TIME = 60 * 60 * 1000

// API 응답 데이터를 FullCalendar events 형식으로 변환
export const transformSchedulesToEvents = (schedules) => {
    console.log(`transformSchedulesToEvents 호출, schedules: `, schedules)
    if (!schedules) return []
    
    return schedules.events?.map(event => ({
        title: event.featureTitle,
        start: event.scheduleDate, // FullCalendar는 'start' 속성을 사용
        date: event.scheduleDate,  // 호환성을 위해 둘 다 포함
        extendedProps: {
            scheduleId: event.scheduleId,
            featureId: event.featureId,
            projectId: event.projectId,
            featureDescription: event.featureDescription,
            allocatedHours: event.allocatedHours,
            category: event.category,
            priorityLevel: event.priorityLevel,
            status: event.status
        }
    })) || []
}

// 사용자 전체 프로젝트 일정 조회 (월별)
export const useUserScheduleByMonth = (year, month) => {
    return useQuery({
        queryKey: SCHEDULE_QUERY_KEYS.byMonth(year, month),
        queryFn: () => getUserSchedule({ year, month }),
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2, // 2시간 캐시
        retry: 2,
        refetchOnWindowFocus: false,
        onError: (error) => {
            console.error('useUserScheduleByMonth Error:', error)
        }
    })
}

// 특정 프로젝트 일정 조회 (월별)
export const useProjectScheduleByMonth = (projectId, year, month) => {
    return useQuery({
        queryKey: SCHEDULE_QUERY_KEYS.projectByMonth(projectId, year, month),
        queryFn: () => getProjectSchedule({ projectId, year, month }),
        enabled: !!projectId,       // projectId가 없을 때 쿼리 방지
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2, // 2시간 캐시
        retry: 2,
        refetchOnWindowFocus: false,
        onError: (error) => {
            console.error('useProjectScheduleByMonth Error:', error)
        }
    })
}
