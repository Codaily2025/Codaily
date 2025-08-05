import { defaultInstance, authInstance } from './axios'

// const schedule_default_url = ''

// 더미 데이터
const dummyScheduleList = [
  {
    baseDate: "2025-08-01",
    startDate: "2025-08-01",
    endDate: "2025-08-01",
    events: [
      {
        projectId: 1,
        scheduleId: 1,
        featureId: 1,
        featureTitle: "회원가입 기능 구현",
        featureDescription: "사용자 입력 폼, 유효성 검사",
        scheduleDate: "2025-08-01",
        allocatedHours: 4,
        category: "회원가입",
        priorityLevel: 2,
        status: "in progress",
      },
      {
        projectId: 1,
        scheduleId: 2,
        featureId: 2,
        featureTitle: "이메일 인증 추가",
        featureDescription: "가입 후 인증 메일 발송",
        scheduleDate: "2025-08-01",
        allocatedHours: 3,
        category: "회원가입",
        priorityLevel: 4,
        status: "todo",
      },
    ],
  },
  {
    baseDate: "2025-08-06",
    startDate: "2025-08-06",
    endDate: "2025-08-06",
    events: [
      {
        projectId: 1,
        scheduleId: 3,
        featureId: 3,
        featureTitle: "로그인 기능 구현",
        featureDescription: "JWT 토큰 발급 및 리프레시 토큰 구현",
        scheduleDate: "2025-08-06",
        allocatedHours: 5,
        category: "로그인",
        priorityLevel: 1,
        status: "in progress",
      },
      {
        projectId: 2,
        scheduleId: 4,
        featureId: 4,
        featureTitle: "로그인 오류 메시지 처리",
        featureDescription: "에러 메시지 UI 개선",
        scheduleDate: "2025-08-06",
        allocatedHours: 2,
        category: "로그인",
        priorityLevel: 5,
        status: "todo",
      },
    ],
  },
  {
    baseDate: "2025-08-14",
    startDate: "2025-08-14",
    endDate: "2025-08-14",
    events: [
      {
        projectId: 2,
        scheduleId: 5,
        featureId: 5,
        featureTitle: "DB 스키마 설계",
        featureDescription: "ERD 작성 및 테이블 생성",
        scheduleDate: "2025-08-14",
        allocatedHours: 6,
        category: "DB 설계",
        priorityLevel: 1,
        status: "completed",
      },
    ],
  },
  {
    baseDate: "2025-08-15",
    startDate: "2025-08-15",
    endDate: "2025-08-15",
    events: [
      {
        projectId: 2,
        scheduleId: 6,
        featureId: 6,
        featureTitle: "프로젝트 구조 세팅",
        featureDescription: "폴더 구조, 라우팅 설정",
        scheduleDate: "2025-08-15",
        allocatedHours: 3,
        category: "프로젝트 초기화",
        priorityLevel: 3,
        status: "completed",
      },
    ],
  },
  {
    baseDate: "2025-08-18",
    startDate: "2025-08-18",
    endDate: "2025-08-18",
    events: [
      {
        projectId: 1,
        scheduleId: 7,
        featureId: 7,
        featureTitle: "비밀번호 재설정 기능",
        featureDescription: "이메일 링크 통한 비밀번호 재설정",
        scheduleDate: "2025-08-18",
        allocatedHours: 4,
        category: "회원가입",
        priorityLevel: 3,
        status: "todo",
      },
      {
        projectId: 3,
        scheduleId: 8,
        featureId: 8,
        featureTitle: "프로필 페이지 UI 구성",
        featureDescription: "기본 정보 보기/수정",
        scheduleDate: "2025-08-18",
        allocatedHours: 3,
        category: "UI 디자인",
        priorityLevel: 6,
        status: "in progress",
      },
    ],
  },
]

// api 함수들
// 사용자가 진행 중인 전체 프로젝트 일정 가져오기
// 응답으로 월별 데이터 -> 달력에서 '>' 버튼 클릭할 때마다 요청 (TODO)
export const getUserSchedule = async () => {
    try {
        // 실제 서버 연동 시
        // const userId = ''                                                            // 확인용 하드코딩
        // const response = await authInstance.get(`/users/${userId}/calendar`)        // JWT 구현 후
        // const response = await defaultInstance.get(`/users/${userId}/calendar`)      // JWT 구현 전
        // return response.data

        // 더미 데이터로 확인
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve(dummyScheduleList)
            }, 500) // 네트워크 지연 시뮬레이션
        })

    } catch (error) {
        console.error('getUserSchedule Error:', error)
        throw new Error(error.response?.data?.message || '사용자 일정 목록을 불러오는데 실패했습니다.')
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
