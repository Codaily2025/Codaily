import { defaultInstance, authInstance } from "./axios"

const project_default_url = 'projects/'

// 더미 데이터 - 사용자 진행 중 프로젝트 목록
const dummyProjectsList = [
    {
        "projectId": 1,
        "title": "TaskFlow 칸반보드 시스템",
        "startDate": "2025-01-01",
        "endDate": "2025-03-15",
        "lastWorkedDate": "2025-01-20",
        "status": "IN_PROGRESS",
        "progressRate": 65,
        "techStacks": ["React", "Node.js", "MongoDB"]
    },
    {
        "projectId": 2,
        "title": "E-commerce 쇼핑몰",
        "startDate": "2024-12-01",
        "endDate": "2025-04-30",
        "lastWorkedDate": "2025-01-15",
        "status": "IN_PROGRESS",
        "progressRate": 40,
        "techStacks": ["Vue.js", "Spring Boot", "MySQL"]
    },
    {
        "projectId": 3,
        "title": "모바일 금융 앱",
        "startDate": "2024-11-15",
        "endDate": "2025-05-30",
        "lastWorkedDate": "2025-01-10",
        "status": "IN_PROGRESS",
        "progressRate": 25,
        "techStacks": ["React Native", "Firebase"]
    },
    {
        "projectId": 4,
        "title": "AI 챗봇 서비스",
        "startDate": "2024-10-01",
        "endDate": "2025-02-28",
        "lastWorkedDate": "2025-01-30",
        "status": "IN_PROGRESS",
        "progressRate": 80,
        "techStacks": ["Python", "TensorFlow", "FastAPI"]
    }
]

// 더미 데이터 - 칸반 탭 필드 목록
const dummyKanbanTabFields = [
    "Backend",
    "Frontend",
    "Testing"
]

const dummyProjectDetail = {}

// 데이터 전처리 함수 - 분리 예정
const preprocessProjectsList = (data) => {
    if (!Array.isArray(data)) return []

    // 필요한 전처리 수행 - 확인용 로직 구현
    return data.map(project => ({
        projectId: project.projectId || 'ProjectId does not exists',
        title: project.title || 'Untitled Project',
        lastWorkedDate: project.lastWorkedDate,
        status: project.status || 'unable to check status',
        progressRate: project.progressRate || 0,
        techStacks: project.techStacks || []
    }))
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
                // 가장 최근 lastWorkedDate을 가진 프로젝트 ID 반환
                const sortedProjects = [...dummyProjectsList].sort(
                    (a, b) => new Date(b.lastWorkedDate) - new Date(a.lastWorkedDate)
                )
                resolve(sortedProjects[0].projectId)
            }, 300)
        })

    } catch (error) {
        console.error('getLastWorkedProjectId Error:', error)
        throw new Error(error.response?.data?.message || '마지막 작업 프로젝트를 찾는데 실패했습니다.')
    }
}

// 칸반 보드 탭 데이터
export const getKanbanTabFields = async (projectId) => {
    try {
        // 실제 서버 연동 시
        // const response = await defaultInstance.get(``)
        // const response = await authInstance.get(``)

        // 더미 데이터로 확인
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve(dummyKanbanTabFields)
            }, 500) // 네트워크 지연 시뮬레이션
        })
        
    } catch (error) {
        console.error('getKanbanTabFields Error: ', error)
        throw new Error(error.response?.data?.message || 'Kanban Tab Fields를 불러오는데 실패했습니다.')
    }
}