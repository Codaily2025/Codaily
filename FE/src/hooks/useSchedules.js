import { useQuery } from '@tanstack/react-query'
import { getUserSchedule } from '../apis/scheduleApi'

// query keys
export const SCHEDULE_QUERY_KEYS = {
    all: ['schedules'],
    monthly: () => [...SCHEDULE_QUERY_KEYS.all, 'monthly'],
    byMonth: (year, month) => [...SCHEDULE_QUERY_KEYS.monthly(), { year, month }],
}

// staleTime 설정
const STALE_TIME = 60 * 60 * 1000

// API 응답 데이터를 FullCalendar events 형식으로 변환
export const transformSchedulesToEvents = (schedules) => {
    if (!schedules || !Array.isArray(schedules)) return []
    
    return schedules.flatMap(schedule => 
        schedule.events?.map(event => ({
            title: event.featureTitle,
            date: event.scheduleDate,
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
    )
}

// 사용자 전체 프로젝트 일정 조회 (월별)
export const useUserScheduleByMonth = () => {
    return useQuery({
        queryKey: SCHEDULE_QUERY_KEYS.monthly(),
        queryFn: getUserSchedule,
        staleTime: STALE_TIME,
        cacheTime: STALE_TIME * 2, // 2시간 캐시
        retry: 2,
        refetchOnWindowFocus: false,
        onError: (error) => {
            console.error('useUserScheduleByMonth Error:', error)
        }
    })
}
