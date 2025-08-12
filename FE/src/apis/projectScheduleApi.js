import { defaultInstance, authInstance, githubInstance } from "./axios"

// 프로젝트 생성 시 일정 정보 등록
export const saveProjectSchedule = async (formData) => {
    try {
        const url = `/projects`
        const response = await authInstance.post(url, { ...formData })
        // console.log('프로젝트 일정 정보 등록: ', response.data)
        return response.data

    } catch (error) {
        console.error('saveProjectSchedule Error: ', error)
        throw new Error(error.response?.data?.message || '프로젝트 일정 정보를 등록(저장)하는데 실패했습니다.')
    }
}