// src/apis/axios.js
import axios from "axios"

// axios 모듈 수정 금지
// 주석 처리만 가능
const BASE_URL = 'http://localhost:8081/api/'

const axiosInstance = (url, options) => {
    const instance = axios.create({ baseURL: url, ...options })
    return instance
}

const axiosAuthInstance = (url, options) => {
    // 저장된 사용자 토큰 가져오기
    const token = localStorage.getItem('authToken') || ''
    const instance = axios.create({
        baseURL: url,
        headers: { Authorization: 'Bearer ' + token },          // JWT Bearer 토큰
        withCredentials: true,                                  // 쿠키 포함
        ...options,
    })
    
    // 요청 인터셉터 - 토큰 자동 갱신
    instance.interceptors.request.use(
        (config) => {
            const currentToken = localStorage.getItem('authToken')
            if (currentToken) {
                config.headers.Authorization = 'Bearer ' + currentToken
            }
            return config
        },
        (error) => {
            return Promise.reject(error)
        }
    )
    
    // 응답 인터셉터 - 401 에러 처리
    instance.interceptors.response.use(
        (response) => response,
        (error) => {
            if (error.response?.status === 401) {
                localStorage.removeItem('authToken')
                window.location.href = '/login'
            }
            return Promise.reject(error)
        }
    )
    
    return instance
}

// TODO: 요청, 응답 인터셉터 구현 예정

export const defaultInstance = axiosInstance(BASE_URL)
export const authInstance = axiosAuthInstance(BASE_URL)