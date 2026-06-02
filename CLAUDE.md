# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Frontend** (in `frontend/`):
```bash
npm install        # install deps
npm run dev        # dev server on :5173
npm run build      # production build → dist/
npm run lint       # eslint
```

**Backend** (in `backend/`):
```bash
mvn package -DskipTests   # build JAR
mvn spring-boot:run       # run locally (needs env vars)
```

**Run frontend against production Railway backend (no local backend needed):**
`frontend/.env.local` with `VITE_API_URL=https://sticker-marker-production.up.railway.app`

## Deploy

- **Backend → Railway**: pushes to `master` trigger a Railway redeploy via `Dockerfile`. Health check at `/actuator/health`.
- **Frontend → GitHub Pages**: `.github/workflows/deploy-frontend.yml` triggers on changes to `frontend/**` or `master` push. Builds with `VITE_API_URL` from GitHub repository variable `vars.VITE_API_URL`.

Required Railway env vars: `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL`.

## Architecture

**Backend** — Spring Boot 4 / Java 17, single module under `backend/src/main/java/com/henrique/stickermarker/`:
- Controllers map to `/auth/**` (public), `/me/**` (authenticated), `/collections/**`, `/public/**`, `/invite/**`
- No `/api` prefix on the backend itself — the Vite dev proxy strips `/api` in dev mode
- Security: `JwtAuthenticationFilter` reads `Authorization: Bearer <token>`, puts `userId` in `authToken.details`. All controllers resolve the current user via `Long userId = (Long) authentication.getDetails()`
- JWT: 15 min access token + 7 day refresh token. Refresh tokens stored in `refresh_tokens` table

**Frontend** — React 19 + Vite, JS (no TS):
- Services (`src/services/`) all use `import.meta.env.VITE_API_URL ?? '/api'` as base — in dev the Vite proxy maps `/api/*` → `http://localhost:8080/*` (stripping the `/api` prefix)
- Auth: access token kept in-memory (`memoryAccessToken` in `AuthContext.jsx`), refresh token in `localStorage`. On mount, `AuthContext` automatically re-hydrates via the stored refresh token
- Routes: `App.jsx` wraps private routes in `<ProtectedRoute>`, base path comes from `import.meta.env.BASE_URL` (set to `/sticker-marker/` in builds)

**Database** — PostgreSQL on Neon (cloud). `spring.jpa.hibernate.ddl-auto=update` handles schema migration automatically.
