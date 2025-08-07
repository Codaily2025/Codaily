import axios from "axios"

// axios 모듈 수정 금지
// 주석 처리만 가능
const BASE_URL = 'http://localhost:8080/api/'

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

// TODO: 요청, 응답 인터셉터 구현 예정

export const defaultInstance = axiosInstance(BASE_URL)
export const authInstance = axiosAuthInstance(BASE_URL)