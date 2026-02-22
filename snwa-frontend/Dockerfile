#FROM node:22-alpine AS builder
#WORKDIR /app
#
#COPY package*.json ./
#RUN npm install
#
#COPY . .
#RUN npm run build
#
##Nginx
#FROM nginx:1.28.1-alpine
#COPY --from=builder /app/build /usr/share/nginx/html/
#COPY nginx.conf /etc/nginx/conf.d/default.conf
#
#EXPOSE 80
#CMD ["nginx", "-g", "daemon off;"]

# ec2 배포용
FROM nginx:1.28.1-alpine
# GitHub Actions가 보낸 파일을 Nginx 경로로 복사만 함
COPY ./build /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]