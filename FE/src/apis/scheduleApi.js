import { defaultInstance, authInstance } from './axios'

// const schedule_default_url = ''

// api 함수들
// 사용자가 진행 중인 전체 프로젝트 일정 가져오기
export const getUserSchedule = async ({ year, month }) => {
    try {
        // 실제 서버 연동 시
        const yearMonth = `${year}-${month.padStart(2, '0')}`
        console.log(`yearMonth: `, yearMonth)
        const response = await authInstance.get(`/calendar`, { params: { yearMonth } })
        return response.data

    } catch (error) {
        console.error('getUserSchedule Error:', error)
        throw new Error(error.response?.data?.message || '사용자 일정 목록을 불러오는데 실패했습니다.')
    }
}

// 개별 프로젝트 일정 가져오기
export const getProjectSchedule = async ({ projectId, year, month }) => {
    try {
        const yearMonth = `${year}-${month.padStart(2, '0')}`;
        const response = await authInstance.get(`/projects/${projectId}/calendar`, { params: { yearMonth } })
        return response.data
    } catch (error) {
        console.error('getProjectSchedule Error:', error)
        throw new Error(error.response?.data?.message || '프로젝트 일정 목록을 불러오는데 실패했습니다. ID: ', projectId)
    }
}

// 데이터 전처리 함수들 - 분리 예정
// api 응답 전처리 언제? 어디서?
// const transformSchedulesToEvents = (schedules) => {
// export const transformSchedulesToEvents = (schedules) => {
//     if (!schedules || !Array.isArray(schedules)) return []
    
//     return schedules.flatMap(schedule => 
//         schedule.events?.map(event => ({
//             title: event.featureTitle,
//             date: event.scheduleDate,
//             extendedProps: {
//                 scheduleId: event.scheduleId,
//                 featureId: event.featureId,
//                 projectId: event.projectId,
//                 featureDescription: event.featureDescription,
//                 allocatedHours: event.allocatedHours,
//                 category: event.category,
//                 priorityLevel: event.priorityLevel,
//                 status: event.status
//             }
//         })) || []
//     )
// }
