# 🏥 HealthTrack Pro

> AI-Powered Personal Health Management System built with Java Spring Boot Microservices

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=flat-square&logo=docker)
![OpenAI](https://img.shields.io/badge/OpenAI-GPT--4o--mini-purple?style=flat-square&logo=openai)
![Redis](https://img.shields.io/badge/Redis-Cache-red?style=flat-square&logo=redis)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)

---

## 🌟 Overview

HealthTrack Pro is a microservices-based health management system that allows patients to:

- 📊 **Track** daily health readings (blood pressure, glucose, pulse, weight)
- 🤖 **Chat** with an AI assistant about their health reports using RAG
- 🔬 **Analyze** medical images (X-rays, scans) using Vision AI
- 📧 **Receive** email reminders and real-time notifications
- 🔐 **Authenticate** securely via JWT or Google OAuth2

---

## 🏗️ Architecture

```text
┌─────────────────────────────────────────────────────┐
│                     Client                          │
│              (Swagger / Frontend)                  │
└──────────┬──────────┬──────────┬──────────┬─────────┘
           │          │          │          │
     :8081 │    :8082 │    :8083 │    :8084 │
           ▼          ▼          ▼          ▼
    ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
    │  Auth    │ │  Health  │ │    AI    │ │  Notify  │
    │ Service  │ │ Service  │ │ Service  │ │ Service  │
    └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
         │            │            │             │
         ▼            ▼            ▼             ▼
      MySQL        MySQL        ChromaDB       Gmail
                   Redis        OpenAI        WebSocket
```

---

## ⚙️ Services

### 🔐 Auth Service — Port 8081

Handles all authentication and authorization.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/rest/auth/register` | Register new user |
| POST | `/rest/auth/login` | Login and get tokens |
| POST | `/rest/auth/refresh` | Refresh access token |
| POST | `/rest/auth/logout` | Logout and revoke token |
| POST | `/rest/auth/activate` | Activate account via OTP |
| POST | `/rest/auth/changePassword` | Change password via OTP |
| GET | `/rest/auth/checkToken` | Validate token |
| GET | `/oauth2/authorization/google` | Login with Google |

**Features:**

- JWT Access Token (24h) + Refresh Token (7 days)
- Token revocation on logout
- Google OAuth2 (OIDC)
- OTP via Email for account activation
- Role-based access (USER, ADMIN, MANAGER)

---

### 💊 Health Service — Port 8082

Manages patient health records.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/health/records` | Add new health record |
| GET | `/api/health/records` | Get all records (paginated) |
| GET | `/api/health/records?type=GLUCOSE` | Filter by type |
| GET | `/api/health/records/range` | Get records by date range |
| GET | `/api/health/records/stats` | Get min/max statistics |
| PUT | `/api/health/records/{id}` | Update a record |
| DELETE | `/api/health/records/{id}` | Delete a record |
| POST | `/api/health/files/upload` | Upload PDF or image |

**Features:**

- Pagination & filtering
- Redis caching with `@Cacheable` / `@CacheEvict`
- File upload (PDF / images)
- JWT authentication via filter

**Record Types:**  
`BLOOD_PRESSURE`, `GLUCOSE`, `PULSE`, `WEIGHT`, `TEMPERATURE`

---

### 🤖 AI Service — Port 8083

AI-powered health assistant using LangChain4j and OpenAI.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/chat` | General health chat |
| POST | `/api/ai/index` | Upload PDF to ChromaDB |
| POST | `/api/ai/ask-reports` | Ask questions about reports |
| POST | `/api/ai/analyze` | Analyze medical images |

**Features:**

- **RAG System:** PDF reports indexed in ChromaDB and queried with semantic search
- **Vision AI:** Medical image analysis via GPT-4o-mini
- **Bilingual:** Supports Arabic and English
- **Facade Pattern:** `AiFacade` routes requests to the correct service

---

### 🔔 Notification Service — Port 8084

Handles email and real-time notifications.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/notify/email` | Send custom email |
| POST | `/api/notify/send` | Send real-time notification |
| POST | `/api/notify/medication` | Send medication reminder |
| POST | `/api/notify/alert` | Send high reading alert |
| WS | `/ws` | WebSocket connection |

**Features:**

- Spring Mail via Gmail SMTP
- WebSocket with STOMP protocol
- HTML email templates
- Real-time notifications per user topic

---

## 🛠️ Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT + OAuth2 |
| AI | LangChain4j + OpenAI GPT-4o-mini |
| Vector DB | ChromaDB |
| Cache | Redis |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| Messaging | WebSocket + STOMP |
| Mail | Spring Mail + Gmail SMTP |
| Docs | Swagger / OpenAPI 3 |
| Build | Maven |
| Container | Docker + Docker Compose |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker Desktop
- Maven 3.9+
- OpenAI API Key
- Gmail App Password
- Google OAuth2 Credentials

### Clone the project

```bash
git clone https://github.com/FATMAadel4/healthtrack-pro.git
cd healthtrack-pro
```

### Configure environment variables

```bash
cp .env.example .env
```

Edit `.env`

```env
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
OPENAI_API_KEY=sk-your-openai-key
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
```

### Run with Docker Compose

```bash
docker-compose up --build
```

### Access services

| Service | URL |
|---------|------|
| Auth Swagger | http://localhost:8081/swagger-ui/index.html |
| Health Swagger | http://localhost:8082/swagger-ui/index.html |
| AI Swagger | http://localhost:8083/swagger-ui/index.html |
| Notification Swagger | http://localhost:8084/swagger-ui/index.html |

---

## 📁 Project Structure

```text
healthtrack-pro/
├── docker-compose.yml
├── init.sql
├── .env
│
├── auth-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/example/auth_service/
│       ├── auth/
│       │   ├── JwtAuthorizationFilter.java
│       │   ├── OAuth2SuccessHandler.java
│       │   └── SecurityConfig.java
│       ├── controllers/AuthController.java
│       ├── services/
│       │   ├── AuthService.java
│       │   ├── JwtService.java
│       │   └── RefreshTokenService.java
│       └── entity/
│           ├── User.java
│           ├── Token.java
│           └── RefreshToken.java
│
├── health-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/example/health_service/
│       ├── controller/HealthRecordController.java
│       ├── service/
│       │   ├── HealthRecordService.java
│       │   └── FileStorageService.java
│       ├── entity/HealthRecord.java
│       ├── repository/HealthRecordRepository.java
│       └── security/JwtFilter.java
│
├── ai-service/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/example/ai_service/
│       ├── controller/AiController.java
│       ├── service/
│       │   ├── AiFacade.java
│       │   ├── RagService.java
│       │   ├── VisionService.java
│       │   └── ChatService.java
│       └── config/LangChainConfig.java
│
└── notification-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/example/notification_service/
        ├── controller/NotificationController.java
        ├── service/
        │   ├── EmailService.java
        │   └── WebSocketService.java
        └── config/WebSocketConfig.java
```

---

## 🔒 Security Flow

```text
1.Register → OTP via Email → Activate Account

2.Login → Access Token + Refresh Token

3.Request → JWT Filter validates token

4.Expired → Refresh Token

5.Logout → Token revoked
```

---

## 👩‍💻 Developer

**Fatma Adel Mohamed**  
Backend Developer (Java & Spring)  
Cairo, Egypt

GitHub: https://github.com/FATMAadel4
