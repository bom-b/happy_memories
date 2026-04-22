# 1단계: React 빌드
FROM node:22-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# 2단계: Nginx를 통한 정적 파일 서빙
FROM nginx:alpine
# 기본 Nginx 설정 삭제 후 커스텀 설정 복사
RUN rm -rf /etc/nginx/conf.d/*
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 빌드된 정적 파일을 Nginx가 서빙할 디렉토리로 복사
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]