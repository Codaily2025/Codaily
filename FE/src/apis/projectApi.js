import { defaultInstance, authInstance } from "./axios"

const project_default_url = 'projects/'

// 데이터 전처리 함수 - 분리 예정
const preprocessProjectsList = (data) => {
    if (!Array.isArray(data)) return []

    // 필요한 전처리 수행 - 확인용 로직 구현
    return data.map(project => ({
        projectId: project.id || 'ProjectId does not exists',
        title: project.name || 'Untitled Project',
        lastWorkedDate: project.lastActivity,
        status: project.status || 'unable to check status',
        progressRate: project.progress || 0,
        dueDate: project.dueDate || '9999-12-31'
    }))
}

// api 함수들
// 사용자가 진행 중인 프로젝트 목록 가져오기 (사이드바용)
export const getUserProjects = async () => {
    try {
        // 실제 서버 연동 시 이런 형태로~
        const response = await authInstance.get(`/users/projects/active`)
        // console.log('사용자가 진행 중인 프로젝트 목록: ', response.data.data.projects)
        return preprocessProjectsList(response.data.data.projects)

    } catch (error) {
        console.error('getUserProjects Error:', error)
        throw new Error(error.response?.data?.message || '프로젝트 목록을 불러오는데 실패했습니다.')
    }
}

// 칸반 보드 탭 데이터
export const getKanbanTabFields = async (projectId) => {
    try {
        // 실제 서버 연동 시
        // const response = await defaultInstance.get(``)
        const response = await authInstance.get(`/projects/${projectId}/features/field-tabs`)
        console.log(response.data)
        return response.data

    } catch (error) {
        console.error('getKanbanTabFields Error: ', error)
        throw new Error(error.response?.data?.message || 'Kanban Tab Fields를 불러오는데 실패했습니다.')
    }
}

// 칸반 보드 탭별 feature_items 데이터
export const getFeatureItemsByKanbanTab = async (projectId, field) => {
    try {
        // api 요청 url
        const url = `projects/${projectId}/features/field/${field}/by-status`
        // console.log('요청 url 확인: ', url)

        // 실제 서버 연동 시 
        // const response = await defaultInstance.get(``)
        const response = await authInstance.get(url)
        return response.data

    } catch (error) {
        console.error('getFeatureItemsByKanbanTab Error: ', error)
        throw new Error(error.response?.data?.message || `${field} 하위 feature_items를 불러오는데 실패했습니다.`)
    }
}

// Feature Item 상태 변경
export const updateFeatureItemStatus = async (projectId, featureId, newStatus) => {
    try {
        // api 요청 url
        const url = `projects/${projectId}/features/${featureId}/status`
        const response = await authInstance.patch(url, { newStatus: newStatus })
        return response.data
        
    } catch (error) {
        console.error('updateFeatureItemStatus Error:', error)
        throw new Error(error.response?.data?.message || '상태 변경에 실패했습니다.')
    }
}

// 부모 기능 리스트 조회
export const getParentFeatures = async (projectId) => {
    try {
        const url = `projects/${projectId}/features/parents`
        const response = await authInstance.get(url)
        return response.data

    } catch (error) {
        console.error('getParentFeatures Error: ', error)
        throw new Error(error.response?.data?.message || `부모 기능 목록을 불러오는데 실패했습니다. 프로젝트 Id: `, projectId)
    }
}

// 수동 기능 추가
export const addFeaturesManually = async (projectId, formData) => {
    try {
        const url = `projects/${projectId}/features`
        const response = await authInstance.post(url, { ...formData })
        return response.data
    } catch (error) {
        console.error('addFeaturesManually Error: ', error)
        throw new Error(error.response?.data?.message || '수동 기능 추가에 실패했습니다.')
    }
}

// 프로젝트 초기 일정 설정
export const createProjectInitialSchedule = async (projectId) => {
    try {
        // projects/{projectId}/features/schedule
        const url = `projects/${projectId}/features/schedule`
        const response = await authInstance.post(url)
        return response.data
    } catch (error) {
        console.error('addFeaturesManually Error: ', error)
        throw new Error(error.response?.data?.message || '수동 기능 추가에 실패했습니다.')
    }
}

// 프로젝트 생성 시 체크리스트 생성
export const createCheckList = async (projectId) => {
    try {
        // feature-checklist/{projectId}/generate
        const url = `feature-checklist/${projectId}/generate`
        const response = await authInstance.get(url)
        return response.data
    } catch (error) {
        console.error('addFeaturesManually Error: ', error)
        throw new Error(error.response?.data?.message || '수동 기능 추가에 실패했습니다.')
    }
}

// 모든 프로젝트의 회고 데이터 조회
export const getAllRetrospectives = async (before = null, limit = 15) => {
    try {
        const params = new URLSearchParams()
        // before 파라미터는 항상 전달 (초기 로딩시 오늘 날짜, 무한스크롤시 마지막 데이터 날짜)
        if (before) {
            params.append('before', before)
        }
        params.append('limit', limit.toString())
        
        const url = `projects/retrospectives?${params.toString()}`
        console.log('모든 프로젝트 회고 조회 요청 api url: ', url)
        const response = await authInstance.get(url)
        return response.data
    } catch (error) {
        console.error('getAllRetrospectives Error: ', error)
        throw new Error(error.response?.data?.message || '회고 데이터를 불러오는데 실패했습니다.')
    }
}

// 특정 프로젝트의 회고 데이터 조회
export const getProjectRetrospectives = async (projectId, before = null, limit = 15) => {
    try {
        const params = new URLSearchParams()
        // before 파라미터는 항상 전달 (초기 로딩시 오늘 날짜, 무한스크롤시 마지막 데이터 날짜)
        if (before) {
            params.append('before', before)
        }
        params.append('limit', limit.toString())
        
        const url = `projects/${projectId}/retrospectives?${params.toString()}`
        console.log('특정 프로젝트 회고 조회 요청 api url: ', url)
        const response = await authInstance.get(url)
        return response.data
    } catch (error) {
        console.error('getProjectRetrospectives Error: ', error)
        throw new Error(error.response?.data?.message || `프로젝트 ${projectId}의 회고 데이터를 불러오는데 실패했습니다.`)
    }
}

// 회고 수동 생성
export const createRetrospective = async (projectId) => {
    try {
        const url = `projects/${projectId}/retrospectives`
        console.log('회고 수동 생성 요청 api url: ', url)
        const response = await authInstance.post(url)
        return response.data
    } catch (error) {
        console.error('createRetrospective Error: ', error)
        throw new Error(error.response?.data?.message || '회고 생성에 실패했습니다.')
    }
}