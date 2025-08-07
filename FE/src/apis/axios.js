import axios from "axios"

const BASE_URL = 'http://localhost:8081/api/'

const axiosInstance = (url, options) => {
    const instance = axios.create({ baseURL: url, ...options })
    return instance
}

const axiosAuthInstance = (url, options) => {
    // 저장된 사용자 토큰 가져오기
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken') || '';
    const instance = axios.create({
        baseURL: url,
        headers: { Authorization: 'Bearer ' + token },          // JWT Bearer 토큰
        ...options,
    })
    return instance
}

export const defaultInstance = axiosInstance(BASE_URL)
export const authInstance = axiosAuthInstance(BASE_URL)