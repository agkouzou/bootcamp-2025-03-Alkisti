# AI Chat App - Inspired by ChatGPT

A full-stack, web-based AI chat platform that allows users to interact with an assistant powered by the Groq API. Built with a modern tech stack, the app emphasizes personalization, secure authentication, and rich chat features.

---

## Tech Stack

- **Backend:** Spring Boot, Spring Security, JWT Authentication
- **Frontend:** Next.js, React (npm)
- **AI Integration:** Groq API
- **Email Services:** SendGrid (verification & password reset)

---

## Getting Started

### Prerequisites
- **Java 21+**
- **Node.js** (with npm)
- **PostgreSQL** running on `localhost:5432` (default database `postgres`)
- **Docker** (optional - only needed for the containerized database setup below)

### Database setup
The backend expects a PostgreSQL instance on `localhost:5432` with a `postgres` database, using the credentials in `application.properties` (`postgres` / `postgres`). Pick one option:

**Option A - Docker (fastest, matches the defaults):**
```bash
docker run --name aichat-postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:17
```
This starts a `postgres` superuser with password `postgres` and a default `postgres` database on port 5432 - no further configuration needed.

**Option B - Native install:**
Install PostgreSQL locally and ensure the `postgres` role's password is `postgres`. If your local setup uses different credentials, update `spring.datasource.username` / `password` (and `url` if needed) in `src/main/resources/application.properties` to match.

### Environment variables
The backend reads secrets from a `.env` file in the project root (loaded automatically at startup). A template, `.env.example`, is provided with the required keys:

```dotenv
GROQ_API_KEY=
SENDGRID_API_KEY=
SENDGRID_FROM_EMAIL=
```

Copy the template to `.env` (which is gitignored, so your secrets stay out of version control) and fill in your own credentials:

```bash
cp .env.example .env
```

Then add your values to `.env`.

### Configuration
Non-secret settings live in `src/main/resources/application.properties`. Defaults target local development - override these for other environments:

| Property | Default | Purpose |
|---|---|---|
| `spring.datasource.url` / `username` / `password` | `localhost:5432`, `postgres` / `postgres` | Database connection |
| `app.cors.allowed-origins` | `http://localhost:3000` | Origins allowed to call the API |
| `app.frontend.base-url` | `http://localhost:3000` | Base URL used in verification/reset email links |

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

### Sharing a temporary public demo (optional)
The frontend proxies API calls to the backend server-side (`next.config.mjs`
rewrites `/api/*` to the backend), so the browser talks to a single origin and no
CORS setup is needed. This makes it easy to expose the app through a tunnel such
as [ngrok](https://ngrok.com) without exposing the backend separately.

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

> A tunnel exposes your locally-running app to the public internet, with whatever
> security posture it currently has. Use it only for short, ad-hoc demos and stop
> the tunnel when you are done.

---

## Core Features

### Authentication & Security
- Secure **email/password login** with JWT
- **Email verification** and **password reset** via SendGrid
- Protected routes based on user session
- JWT expiration handling & validation; single-use email verification/reset tokens

### Chat Interface
- Authenticated users land on their **most recent thread**
- Send messages to the assistant; responses powered by Groq API
- **Message editing** regenerates the assistant's reply
- **Deleting a user message** removes its associated assistant response
- Each user message starts a **new thread**
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

- Modular service-oriented backend using Spring
- DTO layer decoupling entities from chat API responses
- Intuitive stateful frontend with smooth page transitions
- Responsive feedback on UI actions (loading indicators, success messages)
- Minimal page reloads with Next.js routing

---

## Future Improvements

> Not part of the current release, but on the roadmap:
- Account deletion
- Process and extract text from image and PDF files
- Export conversations to PDF/Markdown
- Admin dashboard for managing users and content

---

## Preview

> https://www.loom.com/share/488e19d3c60e4451b924c13b87366984?sid=b28e3ba3-ad92-433c-9059-94292e9170da
