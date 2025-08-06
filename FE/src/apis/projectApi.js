import { defaultInstance, authInstance } from "./axios"

const project_default_url = 'projects/'

// 더미 데이터 - 서버 연동 전까지 사용
const dummyProjectsList = [
    {
        id: 1,
        title: "TaskFlow 칸반보드 시스템",
        lastWorkedAt: "2025-01-20T10:30:00Z",
        status: "active"
    },
    {
        id: 2,
        title: "실시간 채팅 애플리케이션",
        lastWorkedAt: "2025-01-19T15:20:00Z",
        status: "active"
    },
    {
        id: 3,
        title: "E-commerce 쇼핑몰",
        lastWorkedAt: "2025-01-12T09:45:00Z",
        status: "active"
    },
    {
        id: 4,
        title: "블로그 플랫폼",
        lastWorkedAt: "2025-01-10T14:30:00Z",
        status: "active"
    }
]

const dummyProjectDetail = {
    id: 1,
    title: "TaskFlow 칸반보드 시스템",
    description: "현대적인 UI/UX를 가진 프로젝트 관리 도구",
    status: "active",
    createdAt: "2025-01-01T00:00:00Z",
    lastWorkedAt: "2025-01-20T10:30:00Z",
    features: [
        {
            id: 1,
            name: "사용자 인증",
            status: "completed",
            tasks: [
                {
                    id: 1,
                    category: "일반 로그인 구현",
                    title: "JwtTokenProvider 클래스 구현",
                    details: "토큰 생성, 파싱, 검증 메서드 구현",
                    dueDate: "2025/01/30 17:19",
                    status: "completed"
                }
            ]
        },
        {
            id: 2,
            name: "칸반보드 시스템",
            status: "in_progress",
            tasks: [
                {
                    id: 2,
                    category: "칸반보드 구현",
                    title: "드래그 앤 드롭 기능",
                    details: "카드 간 이동 및 상태 변경 기능",
                    dueDate: "2025/02/15 17:19",
                    status: "in_progress"
                },
                {
                    id: 3,
                    category: "칸반보드 구현", 
                    title: "실시간 동기화",
                    details: "WebSocket을 통한 실시간 업데이트",
                    dueDate: "2025/02/20 17:19",
                    status: "todo"
                }
            ]
        }
    ]
}

// 데이터 전처리 함수 - 분리 예정
const preprocessProjectsList = (data) => {
    if (!Array.isArray(data)) return []
    
    return data.map(project => ({
        id: project.id,
        title: project.title || 'Untitled Project',
        lastWorkedAt: project.lastWorkedAt,
        status: project.status || 'active'
    }))
}

const preprocessProjectDetail = (data) => {
    if (!data) return null
    
    return {
        ...data,
        features: data.features || []
    }
}

// api 함수들
// 사용자가 진행 중인 프로젝트 목록 가져오기 (사이드바용)
export const getUserProjects = async () => {
    try {
        // 실제 서버 연동 시 이런 형태로~
        // const response = await authInstance.get(project_default_url + 'user')
        // return preprocessProjectsList(response.data)
        
        // 확인용 더미 데이터
        return new Promise((resolve) => {
            setTimeout(() => {
                const processedData = preprocessProjectsList(dummyProjectsList)
                resolve(processedData)
            }, 500) // 네트워크 지연 시뮬레이션
        })

    } catch (error) {
        console.error('getUserProjects Error:', error)
        throw new Error(error.response?.data?.message || '프로젝트 목록을 불러오는데 실패했습니다.')
    }
}

// 마지막으로 작업한 프로젝트 ID 가져오기
export const getLastWorkedProjectId = async () => {
    try {
        // 실제 서버 연동 시
        // const response = await authInstance.get(project_default_url + 'last-worked-id')
        // return response.data.lastWorkedProjectId
        
        // 확인용 더미 데이터
        return new Promise((resolve) => {
            setTimeout(() => {
                // 가장 최근 lastWorkedAt을 가진 프로젝트 ID 반환
                const sortedProjects = [...dummyProjectsList].sort(
                    (a, b) => new Date(b.lastWorkedAt) - new Date(a.lastWorkedAt)
                )
                resolve(sortedProjects[0].id)
            }, 300)
        })

    } catch (error) {
        console.error('getLastWorkedProjectId Error:', error)
        throw new Error(error.response?.data?.message || '마지막 작업 프로젝트를 찾는데 실패했습니다.')
    }
}

// 특정 프로젝트 상세 정보 가져오기
export const getProjectById = async (projectId) => {
    try {
        // 실제 서버 연동 시
        // const response = await authInstance.get(project_default_url + projectId)
        // return preprocessProjectDetail(response.data)
        
        // 확인용 더미 데이터
        return new Promise((resolve) => {
            setTimeout(() => {
                const projectData = {
                    ...dummyProjectDetail,
                    id: parseInt(projectId)
                }
                const processedData = preprocessProjectDetail(projectData)
                resolve(processedData)
            }, 600)
        })

    } catch (error) {
        console.error('getProjectById Error:', error)
        throw new Error(error.response?.data?.message || '프로젝트 정보를 불러오는데 실패했습니다.')
    }
}