# AI Chat App - Inspired by ChatGPT

A full-stack AI chat platform where users talk to an assistant powered by the Groq API, with JWT authentication, email verification, per-user personalization, and model selection.

---

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4, Spring Security (JWT), Spring Data JPA
- **Frontend:** Next.js 15, React 19
- **Database:** PostgreSQL 17
- **AI Integration:** Groq API (OpenAI-compatible chat completions)
- **Email:** SendGrid (verification & password reset), optionally routed through n8n
- **Testing:** JUnit 5, Mockito, Spring `MockRestServiceServer`

## Project Structure

```
src/main/java/...      Spring Boot backend (controllers, services, JPA entities, DTOs)
src/main/resources/    application.properties and the optional seed script (db/)
src/test/java/...      backend unit tests
bootcamp-frontend/     Next.js frontend (pages, constants, global styles)
docs/                  n8n notification workflow: documentation and importable workflows
```

---

## Getting Started

### Prerequisites
- **Java 21+**
- **Node.js 18.18+** (20 LTS recommended) with npm
- **PostgreSQL** running on `localhost:5432` (default database `postgres`)
- **Docker** (optional - only needed for the containerized database setup below)
- A **Groq API key** - [console.groq.com](https://console.groq.com)
- A **SendGrid account** with a verified sender - only needed for signup verification and password reset emails

### Database setup
The backend expects a PostgreSQL instance on `localhost:5432` with a `postgres` database, using the credentials in `application.properties` (`postgres` / `postgres`). Pick one option:

**Option A - Docker (fastest, matches the defaults):**
```bash
docker run --name aichat-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:17
```
This starts a `postgres` superuser with password `postgres` and a default `postgres` database on port 5432 - no further configuration needed.

**Option B - Native install:**
Install PostgreSQL locally and ensure the `postgres` role's password is `postgres`. If your local setup uses different credentials, update `spring.datasource.username` / `password` (and `url` if needed) in `src/main/resources/application.properties` to match.

### Database initialization
When the backend starts, Hibernate (`spring.jpa.hibernate.ddl-auto=update`) automatically creates/updates the core tables from the JPA entities (`users`, `threads`, `messages`, `user_profile_settings`, ...). **No manual schema step is required to run the app** - you can start the backend against an empty database and register an account through the sign-up flow.

The seed script `src/main/resources/db/1_bootcamp_schema.sql` (pre-verified test users) is **not** run automatically - it lives at a non-default path and is not wired into Spring's SQL initialization. Run it manually only if you want sample data:

```bash
psql -h localhost -p 5432 -U postgres -d postgres -f src/main/resources/db/1_bootcamp_schema.sql
```

The seeded users are **pre-verified**, so you can log in immediately without configuring email - handy for testing the chat flow with just a Groq key. For example, `chris@ctrlspace.dev` / `123456` (see the script for the full list).

> The script begins with `TRUNCATE ... CASCADE`, so it **wipes** the listed tables before seeding. Run it only against a disposable development database.

### Resetting the database
To start over from a clean state, pick whichever fits your setup:

- **Re-run the seed script** - because it begins with `TRUNCATE ... RESTART IDENTITY CASCADE`, running it again clears the chat tables and re-inserts the pre-verified test users:
  ```bash
  psql -h localhost -p 5432 -U postgres -d postgres -f src/main/resources/db/1_bootcamp_schema.sql
  ```
- **Recreate the database from scratch** (empty; Hibernate rebuilds the schema on the next backend start). With Docker:
  ```bash
  docker rm -f aichat-postgres
  docker run --name aichat-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:17
  ```

### Environment variables
The backend reads configuration from a `.env` file in the project root (loaded automatically at startup). A template, `.env.example`, is provided:

```dotenv
GROQ_API_KEY=
SENDGRID_API_KEY=
SENDGRID_FROM_EMAIL=

# Optional (development): comma-separated frontend origins allowed to call the API
# directly, e.g. an ngrok URL. Leave blank to use the default, http://localhost:3000.
CORS_ALLOWED_ORIGINS=

# n8n notifications (production URL: the workflow must be published)
N8N_WEBHOOK_URL=
N8N_WEBHOOK_SECRET=
```

Copy the template to `.env` (which is gitignored, so your secrets stay out of version control) and fill in your values:

```bash
cp .env.example .env
```

| Variable | Needed for |
|---|---|
| `GROQ_API_KEY` | always - chat responses and thread titles |
| `SENDGRID_API_KEY`, `SENDGRID_FROM_EMAIL` | signup verification and password reset emails (not needed when logging in with the seeded users, or when emails go through n8n) |
| `CORS_ALLOWED_ORIGINS` | optional - only when the browser calls the backend directly from an origin other than `http://localhost:3000` (see Configuration) |
| `N8N_WEBHOOK_URL`, `N8N_WEBHOOK_SECRET` | optional - send emails through an n8n workflow instead of directly through SendGrid ([details](docs/n8n-decoupled-notifications.md)) |

### Configuration
There are two distinct kinds of configuration:

- **Committed defaults** - non-secret settings in `src/main/resources/application.properties`, kept in version control and shared by everyone. They target local development.
- **Machine-specific config** - secrets and per-developer overrides that must never be committed. These live in `.env` (loaded automatically) or in environment variables, and take precedence over the committed defaults.

Common settings, their committed defaults, and how to override them per machine:

| Property | Default (committed) | Per-machine override | Purpose |
|---|---|---|---|
| `spring.datasource.url` / `username` / `password` | `localhost:5432`, `postgres` / `postgres` | edit `application.properties` locally | Database connection |
| `app.cors.allowed-origins` | `http://localhost:3000` | `CORS_ALLOWED_ORIGINS` env var | Comma-separated frontend origins allowed to call the API |
| `app.frontend.base-url` | `http://localhost:3000` | run argument (see the demo section) | Base URL used in verification / reset email links |

`app.cors.allowed-origins` is defined as `${CORS_ALLOWED_ORIGINS:http://localhost:3000}`, so setting the `CORS_ALLOWED_ORIGINS` environment variable is the preferred way to add an origin - it keeps machine-specific URLs out of version control instead of editing (and accidentally committing) `application.properties`.

### Running the app

**Backend** (from the project root) - starts on `http://localhost:8080`:
```bash
./mvnw spring-boot:run
```

**Frontend** (from `bootcamp-frontend/`) - starts on `http://localhost:3000`:
```bash
npm install
npm run dev
```

The app opens at the login page - `http://localhost:3000/login` (the root path `/` just redirects there).

### Running tests

```bash
./mvnw test
```

### Run configurations (four variations)
How you run the app varies along two independent choices: **where** you use it (local vs. a public ngrok tunnel) and **how the browser reaches the backend** (through the Next.js `/api` proxy, or calling the backend directly). "Direct" is the only combination that involves CORS.

| # | Where | Frontend -> backend | CORS | App is live at |
|---|---|---|---|---|
| 1 | Local | Direct (default) | committed default allows it | `http://localhost:3000/login` |
| 2 | Local | Proxy (`/api`) | not used | `http://localhost:3000/login` |
| 3 | ngrok, 1 tunnel | Proxy (`/api`) | not used | `https://<random>.ngrok-free.dev/login` |
| 4 | ngrok, 2 tunnels | Direct | must allow the frontend URL | `https://<frontend>.ngrok-free.dev/login` |

All four start the same two servers first (see **Running the app** above): backend with `./mvnw spring-boot:run`, frontend with `npm run dev`. Then:

**1. Local + Direct** (default - nothing extra to configure)
Just start the two servers. The frontend calls `http://localhost:8080` directly, and the committed default `app.cors.allowed-origins=http://localhost:3000` already allows that origin.
Live at: **`http://localhost:3000/login`**

**2. Local + Proxy**
Create `bootcamp-frontend/.env.local` containing `NEXT_PUBLIC_API_URL=/api`, then start the servers. The browser calls `/api/*` on the frontend, which forwards to the backend server-side - no cross-origin request, so no CORS.
Live at: **`http://localhost:3000/login`**

> Variations 1 and 2 look identical from the user's side - same URL, same behavior. The only difference is *how* the frontend reaches the backend, and which one you get is decided by the gitignored `bootcamp-frontend/.env.local`: absent (as in a fresh clone) gives #1 (direct); present with `NEXT_PUBLIC_API_URL=/api` gives #2 (proxy). The proxy mainly matters because it is what enables the single-tunnel ngrok setup in #3.

**3. ngrok + Proxy** (recommended for sharing - a single tunnel)
Set `NEXT_PUBLIC_API_URL=/api` (as in #2), start the servers, then `ngrok http 3000`. Only the frontend is exposed; the backend stays private behind the proxy. Get the public URL from http://127.0.0.1:4040. Full steps are in **Sharing a temporary public demo** below.
Live at: **`https://<random>.ngrok-free.dev/login`**

**4. ngrok + Direct** (advanced - not recommended; the proxy in #3 exists to avoid this)
Expose both servers and let the browser call the backend directly. This needs two tunnels, a frontend rebuild, and CORS:
- Tunnel both ports - on the free plan, one agent via `ngrok start --all` with tunnels defined for `3000` and `8080`.
- Point the frontend at the backend's public URL: set `NEXT_PUBLIC_API_URL=https://<backend>.ngrok-free.dev` in `.env.local`, then rebuild with `npm run build && npm run start` (`NEXT_PUBLIC_*` values are baked in at build time).
- Allow the frontend origin on the backend: `CORS_ALLOWED_ORIGINS=http://localhost:3000,https://<frontend>.ngrok-free.dev` (setting the variable replaces the default, so keep `localhost` in the list if you still use it).
- Caveat: free ngrok shows an interstitial page that can block the browser's API calls to the backend tunnel.
Live at: **`https://<frontend>.ngrok-free.dev/login`**

> For any ngrok variation: on the free plan the URL changes each time ngrok restarts, and to demo email verification / password reset you must also set `app.frontend.base-url` to the public frontend URL (see the demo section).

### Sharing a temporary public demo (optional)
Full steps for variation #3. `next.config.mjs` rewrites `/api/*` to the backend, so only the frontend needs a tunnel ([ngrok](https://ngrok.com)) and no CORS setup is needed.

1. Point the frontend at the proxy by creating `bootcamp-frontend/.env.local`:
   ```dotenv
   NEXT_PUBLIC_API_URL=/api
   ```
2. Start the backend and frontend as usual, then tunnel the frontend port:
   ```bash
   ngrok http 3000
   ```
3. Find your public URL: open ngrok's local dashboard at http://127.0.0.1:4040
   (or read the `Forwarding` line in the ngrok terminal). It looks like
   `https://<random>.ngrok-free.dev`. Open it in a browser (the app lands on
   `https://<random>.ngrok-free.dev/login`) and click through ngrok's one-time
   warning page. On the free plan this URL changes on each restart.
4. Only if you are demoing signup / password reset, start the backend with the
   public URL so email links resolve:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.frontend.base-url=https://<random>.ngrok-free.dev"
   ```

Each developer gets their own ngrok URL, and on the free plan it changes on every restart - don't hard-code or commit it. If you call the backend directly instead of through the proxy (variation #4), add the frontend URL to `CORS_ALLOWED_ORIGINS` in `.env`, keeping `http://localhost:3000` in the list:
```dotenv
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://<random>.ngrok-free.dev
```

> A tunnel exposes your locally-running app to the public internet, with whatever
> security posture it currently has. Use it only for short, ad-hoc demos and stop
> the tunnel when you are done.

---

## Core Features

### Authentication & Security
- **Email/password login** with JWT
- **Email verification** and **password reset** via SendGrid, optionally routed through an [n8n workflow](docs/n8n-decoupled-notifications.md)
- Protected routes based on user session
- JWT expiration handling & validation; single-use email verification/reset tokens

### Chat Interface
- Authenticated users land on their **most recent thread**
- Send messages to the assistant; responses powered by Groq API
- Assistant responses rendered as **Markdown** (bold, lists, code blocks, tables)
- **Message editing** regenerates the assistant's reply
- **Deleting a user message** removes its associated assistant response
- **New Chat** creates a new thread; sending a message with no thread selected creates one too
- Threads and messages have full **timestamp tracking**
    - Threads: `createdAt`
    - Messages: `createdAt` & `updatedAt` (if edited)
- **Groq model selection** via dropdown
- **Smart thread titles** generated using the user's profile context
- Each thread is accessible via `/chat?threadId=ID`

### User Account & Settings
- Profile dropdown with:
    - **Account settings** (edit name/email, personalize user profile settings)
    - **Change password**
    - **Logout**
- User sign-up flow with:
    - Sign up using email and password
    - Verification token sent via email to confirm account
    - Account inactive until verified
    - Verification link validates token
    - Token invalidated after successful verification
- Password reset flow with:
    - "Forgot Password?" link
    - Password reset token sent via email
    - Reset link validates token
    - Token invalidated after use
- All account pages share a **consistent layout and user experience**

---

## Personalization Features

Users can configure their assistant experience through a dedicated settings panel:

- **Profile Settings:**
    - Nickname
    - Job/Role
    - Personal introduction
    - Assistant personality traits
    - Additional notes

- **Assistant Behavior:**
    - Tailors tone and replies to match user settings
    - Auto-generates **thread titles** using personality and conversation history
    - Supports different conversational styles (e.g., witty, encouraging, poetic)

---

## Developer Highlights

- Layered Spring backend (controllers, services, repositories) with a DTO layer decoupling JPA entities from API responses
- Centralized error handling: domain exceptions mapped to consistent JSON error responses by a `@ControllerAdvice`
- Pluggable email delivery: direct SendGrid by default, or an [n8n workflow](docs/n8n-decoupled-notifications.md) behind a shared-secret webhook - selected by configuration, with the same failure semantics on both paths, request timeouts, and an n8n error-alert workflow
- Unit tests for the notification layer (JUnit 5, Mockito, `MockRestServiceServer`)
- Same-origin `/api` proxy in Next.js, so the app can be shared through a single tunnel without CORS changes
- Secrets kept out of version control via `.env`, with committed, non-secret defaults in `application.properties`

---

## Future Improvements

> Not part of the current release, but on the roadmap:
- Account deletion
- Process and extract text from image and PDF files
- Export conversations to PDF/Markdown
- Admin dashboard for managing users and content

---

## Preview

[Demo video (Loom)](https://www.loom.com/share/488e19d3c60e4451b924c13b87366984?sid=b28e3ba3-ad92-433c-9059-94292e9170da)
