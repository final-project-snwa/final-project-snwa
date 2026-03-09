# 1. 공통 실행 환경 (Nginx)
FROM nginx:1.28.1-alpine AS base
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

# 2. 로컬 빌드용 환경
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .

ARG VITE_TOSS_CLIENT_KEY
ENV VITE_TOSS_CLIENT_KEY=$VITE_TOSS_CLIENT_KEY

RUN npm run build

# 3. [로컬용] dev - 위에서 빌드한 결과를 사용
FROM base AS dev
COPY --from=builder /app/build /usr/share/nginx/html/
CMD ["nginx", "-g", "daemon off;"]

# 4. [배포용] prod - GitHub Actions가 보낸 결과물 사용
FROM base AS prod
COPY ./build /usr/share/nginx/html/
CMD ["nginx", "-g", "daemon off;"]