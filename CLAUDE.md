# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SNWA is a multilingual sports news platform with:
- **Backend**: Spring Boot 3.4 (Java 21) REST API
- **Frontend**: React 18 + TypeScript + Vite
- **AI Pipeline**: DeepL translation + Gemini 2.5-flash for summarization/tagging
- **Deployment**: Blue-green Docker deployment via Nginx on EC2

## Commands

### Backend (`snwa-backend/`)
```bash
./gradlew clean build        # Full build
./gradlew test               # Run all tests
./gradlew test --tests "com.team.snwa.snwabackend.InterestServiceTest"  # Single test class
```

### Frontend (`snwa-frontend/`)
```bash
npm install
npm run dev    # Dev server on :3000, proxies /api to localhost:8080
npm run build  # Production build → ./build/
```

### Full Stack (root)
```bash
docker compose up --build -d   # Start all services (accessible at http://localhost:70)
```

## Architecture

### System Components
- **MySQL** → **snwa-backend-blue/green** (port 8080/8081) + **snwa-frontend-blue/green** → **Nginx** (port 70)
- **Prometheus** scrapes `/actuator/prometheus` every 5s → **Grafana** (port 3000)
- Nginx dynamically routes via `nginx/conf/service-url.inc` (updated during blue-green deploy)

### Backend Domain Structure
Domains are under `snwa-backend/src/main/java/com/team/snwa/snwabackend/domain/`:

- `article/` — News articles, search, CRUD
- `crawler/` — Strategy-pattern scrapers (ESPN, SkySports, etc.) using Selenium + Jsoup
- `translation/` — Async pipeline: DeepL translates content, Gemini generates 3-line summary + tags
- `user/` — Auth (JWT 24h), email verification, profiles
- `payment/` / `order/` / `wallet/` — Toss Payments integration, coin system
- `exp/` — Experience/level system (attendance, comment rewards)
- `comment/`, `interest/`, `notification/` — Community and engagement features
- `global/` — Security config, AOP, JWT filters, cross-cutting utilities

### Frontend Structure
- `src/pages/` — Route-level pages
- `src/components/` — Reusable UI (includes `admin/`, `figma/`, `ui/` subdirs)
- `src/contexts/` — React Context for global state
- Path alias `@` → `src/`

### Docker Multi-Stage Build
`DOCKER_TARGET=dev` uses local build artifacts; `DOCKER_TARGET=prod` expects pre-built artifacts injected by CI.

## CI/CD

GitHub Actions (`.github/workflows/gradle.yml`) triggers on merged PRs to `develop`:
1. Builds backend (JDK 21) and frontend (Node 22) in parallel
2. SSHs to EC2, detects active blue/green container
3. Builds and starts the inactive container
4. Health-checks via `/actuator/health` before switching Nginx routing
5. Shuts down old container after cutover

## Environment Variables

Copy `.env` and fill in secrets. Key variables:
- `DOCKER_TARGET` — `dev` (local) or `prod` (CI)
- `DEEPL_API_KEY`, `GEMINI_API_KEY` — AI translation pipeline
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_S3_BUCKET` — Image storage (ap-northeast-2)
- `VITE_TOSS_CLIENT_KEY`, `TOSS_SECRET_KEY` — Payment (test keys available)
- `APP_BASE_URL` — `http://localhost:70` locally
