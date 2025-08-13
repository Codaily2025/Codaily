import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { saveProjectSchedule } from '@/apis/projectScheduleApi'

export const useSaveProjectSchedule = (onSuccessCallback) => {
    // const queryClient = useQueryClient()

    return useMutation({
        mutationFn: ({ formData }) => saveProjectSchedule(formData),
        onSuccess: (data, variables) => {
            console.log('useSaveProjectSchedule 실행, 프로젝트 일정 등록: ', data)
            if (onSuccessCallback) {
                onSuccessCallback(data.projectId, data.specId)
            }
        },
        onError: (error) => {
            console.error('useSaveProjectSchedule Error: ', error)
        }
    })
}