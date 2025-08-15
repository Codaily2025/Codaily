import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { createProjectInitialSchedule, createCheckList } from '@/apis/projectApi'

// 프로젝트 초기 일정 생성
export const useCreateInitialSchedule = () => {
    return useMutation({
        mutationFn: ({ projectId }) => createProjectInitialSchedule(projectId),
        onSuccess: (data, variables) => {
            console.log('useCreateInitialSchedule 실행, 초기 일정 생성 완료: ', data)
        },
        onError: (error) => {
            console.error('useCreateInitialSchedule Error: ', error)
        }
    })
}

// 프로젝트 체크리스트 생성
export const useCreateInitialChecklist = () => {
    return useMutation({
        mutationFn: ({ projectId }) => createCheckList(projectId),
        onSuccess: (data, variables) => {
            console.log('useCreateInitialChecklist 실행, 체크리스트 생성 완료: ', data)
        },
        onError: (error) => {
            console.error('useCreateInitialChecklist Error: ', error)
        }
    })
}