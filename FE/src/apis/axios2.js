// src/apis/axios.js
import axios from "axios";
import { useAuthStore } from "../stores/authStore";

// const BASE_URL = import.meta.env.VITE_BASE_URL_2;  // http://localhost:8080
const BASE_URL = import.meta.env.VITE_BASE_URL;
// 1) 인증이 필요 없는 기본 인스턴스
export const defaultInstance = axios.create({
    baseURL: BASE_URL,
    withCredentials: true,
});

// 2) 인증이 필요한 요청용 인스턴스
export const authInstance = axios.create({
    baseURL: BASE_URL,
    withCredentials: true,
});

// 3) 요청 인터셉터 등록: Zustand에서 토큰을 꺼내와 헤더에 자동 추가
authInstance.interceptors.request.use(
    (config) => {
        const { accessToken } = useAuthStore.getState();
        if (accessToken) {
            config.headers = config.headers ?? {};
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);
