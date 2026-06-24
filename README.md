# AI-Powered Resume Analyzer

A full-stack web application where users upload a resume (PDF) and a job description, and an AI (Claude) analyzes the match — returning a score, matched/missing keywords, strengths, improvements, and a summary.

## Tech Stack

| Layer        | Technology |
|--------------|------------|
| Frontend     | Next.js 14 (App Router), TypeScript, Tailwind CSS, React Query, Axios |
| Backend      | Java 25, Spring Boot 3, Spring Security (JWT), Spring Data JPA |
| AI           | Pluggable: Anthropic Claude, Google Gemini, Groq, or local Ollama |
| Database     | PostgreSQL 16 (schema managed by Flyway) |
| Queue/Cache  | Redis 7 (async job queue for analysis processing) |
| File parsing | Apache PDFBox (resume text extraction) |
| DevOps       | Docker, Docker Compose, GitHub Actions |

## Project Structure

```
ai-powered-resume-analyzer/
├── resume-analyzer-backend/     # Spring Boot API
│   ├── src/main/java/com/resumeanalyzer/
│   │   ├── controller/          # REST endpoints (Auth, Analysis)
│   │   ├── service/              # Business logic (AI, PDF, Queue, Auth, Analysis)
│   │   ├── repository/           # Spring Data JPA repositories
│   │   ├── model/entity/         # JPA entities (User, Analysis)
│   │   ├── model/dto/            # Request/response DTOs
│   │   ├── security/             # JWT auth (filter, util, security config)
│   │   ├── exception/            # Global exception handling
│   │   └── config/                # Redis, AI client config
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/          # Flyway SQL migrations
│   └── docker/Dockerfile
├── resume-analyzer-frontend/    # Next.js app
│   ├── app/
│   │   ├── (auth)/login, register
│   │   ├── analyze/                # Submit + view analysis results
│   │   └── dashboard/              # Analysis history
│   ├── components/                 # UI + feature components
│   ├── hooks/                      # React Query hooks
│   ├── lib/                        # Axios instance, auth helpers
│   ├── types/                      # Shared TypeScript types
│   └── docker/Dockerfile
├── docker-compose.yml
├── .env.example
└── .github/workflows/ci.yml
```

## How It Works

1. User registers/logs in → receives a JWT (24h expiry).
2. User uploads a resume PDF + pastes a job description (`POST /api/analysis/submit`).
3. Backend extracts text from the PDF, saves an `Analysis` row with status `PENDING`, and pushes the job ID onto a Redis queue.
4. A background worker pool picks up the job, calls the configured AI provider with the resume text + job description, and stores the structured result (`match_score`, `matched_keywords`, `missing_keywords`, `strengths`, `improvements`, `summary`).
5. The frontend polls `GET /api/analysis/{id}` (or `/status`) every 3s until the status becomes `COMPLETED` or `FAILED`.
6. Users can see their full history on the dashboard.

Each user is limited to **5 analyses per day** (configurable).

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | No | Create an account, returns JWT |
| POST | `/api/auth/login` | No | Log in, returns JWT |
| POST | `/api/analysis/submit` | Yes | Upload resume PDF + job description (multipart) |
| GET | `/api/analysis/all` | Yes | List all analyses for the current user |
| GET | `/api/analysis/{id}` | Yes | Get a single analysis result |
| GET | `/api/analysis/{id}/status` | Yes | Poll job status |

All responses are wrapped in:
```json
{ "success": true, "data": { ... }, "message": "...", "timestamp": "..." }
```

## Running Locally with Docker (recommended)

**Prerequisites:** Docker Desktop installed and running.

```bash
cd ai-powered-resume-analyzer
cp .env.example .env
```

Edit `.env` and set at least:
- `JWT_SECRET` — any long random string
- `AI_PROVIDER` and the matching API key (see "Choosing an AI Provider" below)

Then start everything:

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Postgres: localhost:5432
- Redis: localhost:6379

Stop everything with `docker compose down` (add `-v` to also delete database/redis volumes).

> **Note:** If your machine has a system-level `ANTHROPIC_BASE_URL` (or other `*_BASE_URL`) environment variable set, it will override the value in `.env` (shell env vars take precedence). Make sure it's either unset or matches the value in `.env.example`.

## Choosing an AI Provider

The backend supports multiple AI providers behind a common interface. Pick one with the `AI_PROVIDER` env var:

| `AI_PROVIDER` | Notes |
|----------------|-------|
| `anthropic` (default) | Claude API. Requires `ANTHROPIC_API_KEY` and an account with available credit. |
| `gemini` | Google Gemini. Free tier available — get a key at https://aistudio.google.com/apikey and set `GEMINI_API_KEY`. |
| `groq` | Groq's OpenAI-compatible API. Free tier with generous rate limits — get a key at https://console.groq.com/keys and set `GROQ_API_KEY`. |
| `ollama` | Runs against a local Ollama instance — no API key needed. Install Ollama, run `ollama pull llama3` (or your chosen `OLLAMA_MODEL`), and make sure it's running on the host. Docker Compose is pre-configured to reach it via `host.docker.internal`. |

Only the configured provider's bean is created at startup, so you only need to set the API key for the provider you select.

## Running Locally Without Docker

### Backend

Requires Java 25 and Maven, plus a running Postgres + Redis.

```bash
cd resume-analyzer-backend
cp .env.example .env   # then export the variables, or set them in your IDE run config
mvn spring-boot:run
```

The backend runs on `http://localhost:8080`. Flyway will automatically create the schema on startup.

### Frontend

Requires Node.js 20+.

```bash
cd resume-analyzer-frontend
cp .env.local.example .env.local
npm install
npm run dev
```

The frontend runs on `http://localhost:3000`.

## Environment Variables Reference

See [.env.example](.env.example) for the full list (database credentials, JWT secret/expiry, CORS origins, daily rate limit, and AI provider config for Anthropic/Gemini/Groq/Ollama).

## CI/CD

`.github/workflows/ci.yml` runs on every push/PR to `main`:
- **Backend job**: `mvn clean verify` + Docker image build
- **Frontend job**: `npm run lint`, `npm run build` + Docker image build

## Database Schema

Managed via Flyway migration [V1__init_schema.sql](resume-analyzer-backend/src/main/resources/db/migration/V1__init_schema.sql):

- **users**: `id`, `email`, `password` (BCrypt hash), `name`, `daily_analysis_count`, `last_analysis_date`, `created_at`
- **analyses**: `id`, `user_id`, `resume_text`, `job_description`, `match_score`, `matched_keywords`/`missing_keywords`/`strengths`/`improvements` (JSONB), `summary`, `status` (`PENDING`/`PROCESSING`/`COMPLETED`/`FAILED`), `error_message`, `created_at`

## Security Notes

- Passwords hashed with BCrypt
- JWT-based stateless auth (24h expiry), all endpoints except `/api/auth/**` require a valid token
- Per-user data isolation (analyses are scoped by `user_id`)
- File upload validation: PDF only, max 5MB, rejects encrypted/empty PDFs
- Rate limiting: 5 analyses/user/day
