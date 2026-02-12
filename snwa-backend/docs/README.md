# ⚽ SNWA (Sports News World Wide)

---

## 1. 프로젝트 개요
**SNWA**는 전 세계의 다양한 스포츠 뉴스를 한곳에서 모아보고, 언어의 장벽 없이 즐길 수 있도록 돕는 글로벌 스포츠 뉴스 플랫폼입니다. ESPN, SkySports 등 해외 유력 언론사의 뉴스를 크롤링하여 AI 기반 번역 및 요약 서비스를 제공하며, 사용자 반응형 커뮤니티 기능을 통해 스포츠 팬들의 소통을 지원합니다.

---

## 2. 기술 스택

| 구분 | 기술 스택 | 비고 |
| :--- | :--- | :--- |
| **백엔드** | Spring Boot 3.4, Java 21 | 비즈니스 로직 및 RESTful API |
| **프론트엔드** | React 18, Vite | TypeScript 기반 SPA UI/UX |
| **데이터베이스** | MySQL 8.0 | 데이터 영속성 관리 (JPA, QueryDSL) |
| **AI & 번역** | Spring AI (Gemini), DeepL API | 기사 요약 및 고품질 번역 |
| **크롤링** | Selenium, Jsoup | 해외 스포츠 뉴스 데이터 수집 |
| **프록시** | Nginx | 리버스 프록시 및 정적 파일 서빙 |
| **컨테이너** | Docker, Docker Compose | 마이크로서비스 환경 격리 및 배포 |
| **모니터링** | Prometheus, Grafana | 시스템 상태 및 성능 모니터링 |
| **보안** | Spring Security, JWT | 사용자 인증 및 관리자 권한 제어 |

---

## 📄 API 명세
[03-api-spec.md](03-api-spec.md)

---

## 3. 디렉토리 구조

프로젝트 루트 디렉토리(`final-project-snwa`)를 기준으로 한 구조입니다.

```text
final-project-snwa
├─ snwa-backend/                # Spring Boot 백엔드
│  ├─ docs/                     # 프로젝트 문서
│  │  ├─ 01-project-design.md
│  │  ├─ 02-dependencies.md
│  │  ├─ 03-api-spec.md
│  │  └─ README.md
│  ├─ src/
│  │  ├─ main/java/com/team/snwa/snwabackend/
│  │  │  ├─ domain/             # 도메인별 로직 (article, crawler, user 등)
│  │  │  └─ global/             # 전역 설정 (config, error, security)
│  └─ Dockerfile
├─ snwa-frontend/               # React 프론트엔드
│  ├─ src/
│  │  ├─ components/            # UI 컴포넌트
│  │  ├─ pages/                 # 페이지 라우트
│  │  └─ context/               # 전역 상태 관리
│  └─ Dockerfile
├─ nginx/                       # Nginx 설정
├─ docker-compose.yml           # 전체 서비스 오케스트레이션
└─ .env                         # 환경 변수 설정
```

---
# 로컬 개발 환경 실행 가이드

Docker Compose를 통해 전체 시스템을 손쉽게 구동할 수 있습니다.

## 1. 백엔드 개발 환경 실행
```bash
# 백엔드 디렉토리로 이동
cd snwa-backend

# 프로젝트 빌드 및 컨테이너 실행
docker compose up --build
```

## 2. 프론트엔드 개발 환경 실행
```bash
# 프론트엔드 디렉토리로 이동
cd snwa-frontend

# 개발 서버 실행
npm install
npm run dev
```

## 3. 전체 시스템 통합 실행
```bash
# 프로젝트 루트(final-project-snwa) 디렉토리에서 실행
docker compose up --build -d
```
### 접근 주소 : http://localhost (Nginx 포워딩)
