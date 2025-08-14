import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { addFeaturesManually } from '@/apis/projectApi'

export const useAddFeatures = () => {
    // const queryClient = useQueryClient()

    return useMutation({
        mutationFn: ({ projectId, formData }) => addFeaturesManually(projectId, formData),
        onSuccess: (data, variables) => {
            console.log('useAddFeatures 실행, 수동으로 기능 추가: ', data)            
        },
        onError: (error) => {
            console.error('useAddFeatures Error: ', error)
        }
    })
}