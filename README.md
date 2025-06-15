# 🧠 AI Chat App — Inspired by ChatGPT

A full-stack, web-based AI chat platform that allows users to interact with an assistant powered by the Groq API. Built with a modern tech stack, the app emphasizes personalization, secure authentication, and rich chat features.

---

## 🚀 Tech Stack

- **Backend:** Spring Boot, Spring Security, JWT Authentication
- **Frontend:** Next.js, React (npm)
- **AI Integration:** Groq API
- **Email Services:** SendGrid (verification & password reset)

---

## ✅ Core Features

### 🔐 Authentication & Security
- Secure **email/password login** with JWT
- **Email verification** and **password reset** via SendGrid
- Protected routes based on user session
- Token expiration handling & validation

### 💬 Chat Interface
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

### 🧑 User Account & Settings
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

## 🎨 Personalization Features

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

## 🛠️ Developer Highlights

- Modular service-oriented backend using Spring
- DTO-layer decoupling for clean API responses
- Intuitive stateful frontend with smooth page transitions
- Responsive feedback on UI actions (loading indicators, success messages)
- Minimal page reloads with Next.js routing

---

## 📌 Future Improvements

> Not part of the current release, but on the roadmap:
- Account deletion
- Process and extract text from image and PDF files
- Export conversations to PDF/Markdown
- Admin dashboard for managing users and content

---

## 📷 Preview

> https://www.loom.com/share/488e19d3c60e4451b924c13b87366984?sid=b28e3ba3-ad92-433c-9059-94292e9170da
