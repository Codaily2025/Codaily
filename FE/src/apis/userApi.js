import { defaultInstance, authInstance } from './axios'

// (최초 로그인) 사용자 추가 정보 업데이트
export const updateUserAdditionalInfo = async (formData) => {
  try {
    // const response = await defaultInstance.put('/user/additional-info', formData, {
    const response = await authInstance.put('/user/additional-info', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
    
  } catch (error) {
    console.error('updateUserAdditionalInfo API Error:', error)
    throw new Error(error.response?.data?.message || `추가 정보 저장에 실패했습니다.`)
  }
}

// // 사용자 정보 조회
// export const getUserInfo = async () => {
//   try {
//     const response = await apiClient.get('/api/user/me')
//     return response.data
//   } catch (error) {
//     console.error('getUserInfo API Error:', error)
//     throw error
//   }
// }