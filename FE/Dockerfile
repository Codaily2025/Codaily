# ---------- Build Stage ----------
FROM node:18-alpine AS build
WORKDIR /app

# 의존성
COPY package*.json ./
RUN npm install

# Vite 빌드 타임 ENV
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

COPY . .
RUN npm run build

# ---------- Runtime Stage ----------
FROM nginx:alpine
# 정적 결과물 배포
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
