# ---------- Build Stage ----------
FROM node:18-alpine AS build
WORKDIR /app

# 의존성
COPY package*.json ./
RUN npm install   

# Vite 빌드 타임 ENV를 build-arg로 받기
ARG VITE_GITHUB_CLIENT_ID
ARG VITE_GITHUB_SCOPE
ARG VITE_GITHUB_REDIRECT_URI
ARG VITE_BASE_URL
ARG VITE_BASE_URL_2
ARG VITE_BASE_URL_3

ENV VITE_GITHUB_CLIENT_ID=${VITE_GITHUB_CLIENT_ID} \
    VITE_GITHUB_SCOPE=${VITE_GITHUB_SCOPE} \
    VITE_GITHUB_REDIRECT_URI=${VITE_GITHUB_REDIRECT_URI} \
    VITE_BASE_URL=${VITE_BASE_URL} \
    VITE_BASE_URL_2=${VITE_BASE_URL_2} \
    VITE_BASE_URL_3=${VITE_BASE_URL_3}

# 소스 복사 & 빌드
COPY . .
RUN npm run build

# ---------- Runtime Stage ----------
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
