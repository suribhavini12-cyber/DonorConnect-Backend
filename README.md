# DonorConnect — Backend API

DonorConnect Backend is a production-ready RESTful service built with **Spring Boot 3.4.1** and **Java 17**. It provides secure identity management, blood request tracking, potential donor matching, coordination lifecycle handling, admin moderation, and a grounded Gemini AI Retrieval-Augmented Generation (RAG) blood donation informational chatbot.

---

## Table of Contents

- [Overview & Architecture](#overview--architecture)
- [Key Features](#key-features)
- [Technologies & Dependencies](#technologies--dependencies)
- [Prerequisites](#prerequisites)
- [Database Setup & Configuration](#database-setup--configuration)
- [Environment Variables](#environment-variables)
- [Running Locally](#running-locally)
- [Running Tests](#running-tests)
- [AI & RAG Architecture](#ai--rag-architecture)
- [API Endpoints & Swagger Documentation](#api-endpoints--swagger-documentation)
- [Backend Folder Structure](#backend-folder-structure)
- [Docker & Containerization](#docker--containerization)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

---

## Overview & Architecture

DonorConnect facilitates voluntary blood donation coordination between individuals in need and willing donors. The backend architecture follows clean layered design:

```
┌────────────────────────────────────────────────────────┐
│                   REST Controllers                     │
│  (Auth, Donor, Requests, Matching, Admin, AI, Reports) │
├────────────────────────────────────────────────────────┤
│                    Security Filter                     │
│       (JWT Authentication, Role-Based Authorization)   │
├────────────────────────────────────────────────────────┤
│                     Service Layer                      │
│ (Business logic, Transaction Management, Caching, RAG) │
├────────────────────────────────────────────────────────┤
│                    Repository Layer                    │
│             (Spring Data JPA & Hibernate)              │
├────────────────────────────────────────────────────────┤
│                     MySQL Database                     │
└────────────────────────────────────────────────────────┘
```

> **Medical Safety Disclaimer**: DonorConnect is an informational and logistical coordination platform. Donor matching indicates potential match opportunities only. The platform does **not** evaluate medical eligibility, clinical compatibility, or guarantee blood availability. Clinical validation and donation procedures belong exclusively to certified hospitals, blood banks, and licensed medical professionals.

---

## Key Features

1. **Authentication & Authorization**:
   - Stateless JWT authentication (`io.jsonwebtoken` JJWT 0.12.6).
   - Role-Based Access Control (RBAC): `ROLE_DONOR`, `ROLE_REQUESTER`, `ROLE_ADMIN`.
   - BCrypt password hashing and custom UserDetailsService.

2. **Blood Request Lifecycle Tracking**:
   - Complete request state flow: `PENDING_REVIEW` → `OPEN` → `PARTIALLY_RESPONDED` → `FULFILLED` / `CANCELLED` / `CLOSED`.
   - Immutable audit trail via `RequestEvent` timeline logging every status transition.

3. **Potential Donor Matching**:
   - Matches active, consented donors based on exact blood group compatibility and hospital location.

4. **Donor Response & Coordination**:
   - Full response lifecycle: `INTERESTED` → `CONTACTED` → `CONFIRMED_BY_REQUESTER` → `WITHDRAWN`.

5. **Admin Dashboard & Moderation**:
   - System metrics (total users, active donors, open requests, incident reports).
   - User account activation/deactivation.
   - Request review and moderation.
   - User report review and status resolution (`OPEN`, `UNDER_REVIEW`, `RESOLVED`, `DISMISSED`).

6. **Gemini AI & RAG Chatbot**:
   - **Request Summary**: Generates concise, structured summaries of blood requests.
   - **Duplicate Detection**: Advisory analysis comparing new requests against open requests.
   - **RAG Chatbot**: Answers blood donation queries grounded in verified markdown documents with automatic graceful fallbacks and emergency symptom detection.

7. **Caching & Async Processing**:
   - Spring Cache abstraction for fast retrieval of open requests and matches.
   - Asynchronous background task execution via Spring `@Async`.

8. **OpenAPI & Swagger UI**:
   - Interactive API testing with JWT Bearer authorization support.

---

## Technologies & Dependencies

All dependencies are verified from [`pom.xml`](pom.xml):

- **Java Version**: 17
- **Framework**: Spring Boot 3.4.1
- **Core Starters**:
  - `spring-boot-starter-web`: MVC RESTful architecture.
  - `spring-boot-starter-data-jpa`: Object-Relational Mapping (Hibernate).
  - `spring-boot-starter-security`: Authentication and access control.
  - `spring-boot-starter-validation`: Bean validation constraints (`@Valid`, `@NotBlank`, etc.).
  - `spring-boot-starter-cache`: In-memory caching abstraction.
  - `spring-boot-starter-actuator`: Health check and system metrics.
- **Security & JWT**:
  - `io.jsonwebtoken:jjwt-api:0.12.6`
  - `io.jsonwebtoken:jjwt-impl:0.12.6`
  - `io.jsonwebtoken:jjwt-jackson:0.12.6`
- **Database Driver**:
  - `com.mysql:mysql-connector-j` (MySQL 8 connector)
- **API Documentation**:
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5` (OpenAPI 3 / Swagger UI)
- **Testing & Coverage**:
  - `spring-boot-starter-test` (JUnit 5, Mockito, AssertJ)
  - `spring-boot-test-autoconfigure`
  - `jacoco-maven-plugin:0.8.12` (Code coverage)

---

## Prerequisites

Before setting up the project, ensure you have:

- **Java Development Kit (JDK)**: **Java 17** or higher (e.g., Eclipse Temurin 17, OpenJDK 17).
- **Maven**: Version 3.8+ (The project includes the Maven Wrapper `mvnw`, so a global Maven installation is optional).
- **MySQL Server**: MySQL 8.0+ running locally on port 3306 (or via Docker Compose on port 3307).

Verify your Java version:
```bash
java -version
```

---

## Database Setup & Configuration

1. **Start MySQL Server** and open your MySQL client (MySQL CLI, Workbench, or DBeaver).
2. **Create the database**:
   ```sql
   CREATE DATABASE donorconnect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Hibernate will automatically create and update the required tables on application startup because `spring.jpa.hibernate.ddl-auto` is set to `update`.

---

## Environment Variables

The backend uses sensible local development defaults in [`application.properties`](src/main/resources/application.properties) that can be overridden via environment variables or a `.env` file:

| Environment Variable | Description | Default Fallback Value |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC URL for MySQL database | `jdbc:mysql://localhost:3306/donorconnect` |
| `SPRING_DATASOURCE_USERNAME` | MySQL database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | MySQL database password | `root` |
| `JWT_SECRET` | Base64-encoded 256-bit secret key for JWT signing | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `JWT_EXPIRATION` | Token validity duration in milliseconds | `86400000` (24 hours) |
| `AI_API_KEY` | Google Gemini API key (optional) | *(empty — triggers safe fallback)* |
| `AI_GEMINI_MODEL` | Gemini LLM model identifier | `gemini-3.8-flash` |
| `AI_GEMINI_BASE_URL` | Gemini API endpoint URL | `https://generativelanguage.googleapis.com/v1beta/models/` |

> **Security Note**: Never commit actual database passwords or Gemini API keys to version control. The `.env` file is excluded by `.gitignore`.

---

## Running Locally

Navigate to the `backend` directory:
```bash
cd backend
```

### On Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

### On macOS / Linux:
```bash
chmod +x ./mvnw
./mvnw spring-boot:run
```

When successfully started, the application listens on port **8080**:
```
Tomcat started on port 8080 (http) with context path '/'
Started BackendApplication in X.XXX seconds
```

### Verifying Application Health:
Open your browser or run:
```bash
curl http://localhost:8080/actuator/health
# Response: {"status":"UP"}
```
```bash
curl http://localhost:8080/api/hello
# Response: Hello from DonorConnect Backend!
```

---

## Running Tests

The test suite includes 16 unit and integration test classes testing authentication, controllers, services, cache, and RAG retrieval.

### Run All Tests:
```bash
# Windows
.\mvnw.cmd test

# macOS / Linux
./mvnw test
```

### Generate Code Coverage Report (JaCoCo):
```bash
# Windows
.\mvnw.cmd jacoco:report

# macOS / Linux
./mvnw jacoco:report
```
The HTML coverage report will be generated at `target/site/jacoco/index.html`.

---

## AI & RAG Architecture

The backend includes a grounded Retrieval-Augmented Generation (RAG) system for blood donation educational assistance:

```
User Query ──► Emergency Check ──► RagRetriever (Keyword & Topic Scoring)
                                          │
                        ┌─────────────────┴─────────────────┐
                 Chunks Retrieved                     No Chunks Found
                        │                                    │
               Gemini API Configured?                 "Insufficient Info"
                 ├── Yes ──► Grounded Gemini Prompt
                 └── No  ──► Knowledge Base Fallback
```

### 1. Document Knowledge Base
Documents are loaded dynamically from `classpath:rag/*.md`:
- `what-is-blood-donation.md`
- `donation-process.md`
- `preparation-and-safety.md`
- `before-during-after-donation.md`
- `blood-components-terminology.md`
- `when-to-contact-healthcare.md`
- `donorconnect-platform-guide.md` (Covers platform workflows and request tracking)

### 2. Retrieval & Scoring (`RagRetriever.java`)
- Tokenizes queries, strips stop-words, and applies word normalization.
- Scores chunks based on content, title, and topic weights.
- Enforces a minimum relevance threshold (`minThreshold = 3.0`) to prevent hallucinated answers.

### 3. Emergency Safety Check (`RagService.java`)
- Intercepts acute medical emergency keywords (e.g., chest pain, shortness of breath, heavy bleeding, loss of consciousness).
- Returns an immediate medical advisory to contact emergency services (108/911) or visit the nearest emergency room.

### 4. Zero-Key Fallback Protection
- If `AI_API_KEY` is not provided or Gemini returns an error, the service automatically falls back to relevant text chunks directly from the knowledge base, setting `sourceType: "FALLBACK"`.

---

## API Endpoints & Swagger Documentation

### Interactive Swagger UI
Explore and test all REST endpoints directly in your browser:
* **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
* **OpenAPI 3.1 JSON**: `http://localhost:8080/v3/api-docs`

Click the **Authorize** button in Swagger UI and provide your JWT token (`Bearer <your_token>`) to test protected endpoints.

---

### Core Endpoint Summary

#### 1. Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register a new user (`DONOR`, `REQUESTER`, or `ADMIN`) |
| `POST` | `/api/auth/login` | Public | Authenticate and obtain JWT token |
| `GET` | `/api/auth/me` | Authenticated | Retrieve authenticated user profile |

#### 2. Donor Profile (`/api/donor/profile`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/donor/profile` | `ROLE_DONOR` | Create donor profile (blood group, city, availability) |
| `GET` | `/api/donor/profile` | `ROLE_DONOR` | Retrieve current donor profile |
| `PUT` | `/api/donor/profile` | `ROLE_DONOR` | Update donor profile |
| `PATCH` | `/api/donor/profile/participation` | `ROLE_DONOR` | Toggle status (`ACTIVE` / `PAUSED`) |

#### 3. Blood Requests (`/api/requests`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/requests` | `ROLE_REQUESTER` | Create a new blood request (`PENDING_REVIEW`) |
| `GET` | `/api/requests` | Authenticated | Get all active blood requests |
| `GET` | `/api/requests/my` | `ROLE_REQUESTER` | Get requests created by current user |
| `GET` | `/api/requests/{id}` | Authenticated | Get details of a specific request |
| `PUT` | `/api/requests/{id}` | `ROLE_REQUESTER` | Update request details (if open/pending) |
| `DELETE`| `/api/requests/{id}` | `ROLE_REQUESTER` | Cancel blood request |
| `PATCH`| `/api/requests/{id}/status` | `ROLE_REQUESTER` | Transition status (`FULFILLED`, `CANCELLED`) |
| `GET` | `/api/requests/{id}/timeline` | Authenticated | Get chronological state transition events |
| `GET` | `/api/requests/{id}/matches` | `ROLE_REQUESTER` | Find potential matching donors |

#### 4. Donor Responses (`/api/requests/{requestId}/responses` & `/api/responses`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/requests/{requestId}/responses` | `ROLE_DONOR` | Express interest in donating (`INTERESTED`) |
| `GET` | `/api/requests/{requestId}/responses` | `ROLE_REQUESTER` | View responses received for a request |
| `GET` | `/api/responses/my` | `ROLE_DONOR` | View my submitted interest responses |
| `GET` | `/api/responses/{id}` | Authenticated | Get response by ID |
| `PATCH`| `/api/responses/{id}` | Authenticated | Update status (`CONTACTED`, `CONFIRMED`, `WITHDRAWN`) |

#### 5. Reports (`/api/reports`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/reports` | Authenticated | Submit report on user or request misconduct |

#### 6. Administration (`/api/admin`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | `ROLE_ADMIN` | View platform usage and moderation statistics |
| `GET` | `/api/admin/users` | `ROLE_ADMIN` | List all registered users |
| `PATCH`| `/api/admin/users/{userId}/status` | `ROLE_ADMIN` | Activate or deactivate a user account |
| `GET` | `/api/admin/requests` | `ROLE_ADMIN` | List all platform requests |
| `PATCH`| `/api/admin/requests/{id}/status` | `ROLE_ADMIN` | Moderate request status (`OPEN`, `CLOSED`, etc.) |
| `GET` | `/api/admin/reports` | `ROLE_ADMIN` | List user reports |
| `PATCH`| `/api/admin/reports/{id}` | `ROLE_ADMIN` | Update report status (`UNDER_REVIEW`, `RESOLVED`, `DISMISSED`) |

#### 7. AI & Chatbot (`/api/ai`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/ai/request-summary` | Authenticated | Generate AI structured request summary |
| `POST` | `/api/ai/duplicate-check` | Authenticated | Perform AI duplicate request detection advisory |
| `POST` | `/api/ai/chat` | Authenticated | Ask RAG donation information chatbot |

---

## Backend Folder Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/donorconnect/
│   │   │   ├── BackendApplication.java    # Application entry point (@SpringBootApplication)
│   │   │   ├── ai/                        # AI & Gemini integration
│   │   │   │   ├── AiController.java      # REST endpoints for summary, duplicate, and chat
│   │   │   │   ├── AiService.java         # AI service interface
│   │   │   │   ├── GeminiAiService.java   # Google Gemini REST client with retry logic
│   │   │   │   └── rag/                   # Retrieval-Augmented Generation implementation
│   │   │   │       ├── ChatRequest.java
│   │   │   │       ├── ChatResponse.java
│   │   │   │       ├── RagChunk.java
│   │   │   │       ├── RagChunker.java
│   │   │   │       ├── RagDocument.java
│   │   │   │       ├── RagKnowledgeBase.java
│   │   │   │       ├── RagRetriever.java
│   │   │   │       ├── RagService.java
│   │   │   │       └── SourceDto.java
│   │   │   ├── config/                    # Security, CORS, Swagger, Cache configuration
│   │   │   │   ├── AdminUserSeeder.java   # Seeds default admin account on startup
│   │   │   │   ├── OpenApiConfig.java     # Swagger / OpenAPI 3.1 specification
│   │   │   │   └── SecurityConfig.java    # SecurityFilterChain, CORS, and password encoder
│   │   │   ├── controller/                # REST API controllers
│   │   │   ├── dto/                       # Request and response data transfer objects
│   │   │   ├── exception/                 # GlobalExceptionHandler & custom exceptions
│   │   │   ├── model/                     # JPA entities & enumerations
│   │   │   ├── repository/                # Spring Data JPA repositories
│   │   │   ├── security/                  # JWT filter, service, and UserPrincipal
│   │   │   └── service/                   # Business logic services
│   │   └── resources/
│   │       ├── application.properties     # Application configuration
│   │       └── rag/                       # Approved knowledge-base markdown documents
│   └── test/                              # Comprehensive JUnit & Mockito test suites
├── Dockerfile                             # Multi-stage production container build
├── .dockerignore                          # Excluded files for Docker build
├── .gitignore                             # Excludes target/, .env, and secrets
├── pom.xml                                # Maven build descriptor
├── mvnw                                   # Linux/macOS Maven wrapper
└── mvnw.cmd                               # Windows Maven wrapper
```

---

## Docker & Containerization

The backend can be built and run as a standalone container:

### 1. Build Docker Image
```bash
docker build -t donorconnect-backend .
```

### 2. Run Container
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/donorconnect \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  --name donorconnect-backend \
  donorconnect-backend
```

*(Alternatively, use the root `docker-compose.yml` to start MySQL, backend, and frontend together).*

---

## Troubleshooting

### 1. Database Connection Refused (`CommunicationsException`)
* **Cause**: MySQL service is not running or port 3306 is not open.
* **Fix**: Ensure MySQL is running (`net start MySQL80` on Windows, or `sudo systemctl status mysql` on Linux). Verify the database `donorconnect` exists.

### 2. Access Denied for User `root`
* **Cause**: Incorrect MySQL credentials.
* **Fix**: Provide your actual credentials via environment variables:
  ```powershell
  $env:SPRING_DATASOURCE_USERNAME="your_user"
  $env:SPRING_DATASOURCE_PASSWORD="your_password"
  .\mvnw.cmd spring-boot:run
  ```

### 3. Port 8080 Already in Use
* **Cause**: Another application or previous backend instance is occupying port 8080.
* **Fix**:
  - Identify process: `netstat -ano | findstr :8080`
  - Terminate process: `taskkill /PID <PID> /F` (Windows) or `kill -9 <PID>` (Linux/macOS).

### 4. JWT Secret Key Weak or Missing
* **Cause**: Custom `JWT_SECRET` must be a valid 256-bit (or longer) base64-encoded string.
* **Fix**: Use the provided default development key in `application.properties` or generate a 256-bit key:
  ```bash
  openssl rand -base64 32
  ```

---

## Contributing

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/backend-improvement
   ```
2. Write clean code adhering to existing package architecture.
3. Add unit or integration tests for any new features or services.
4. Verify all tests pass:
   ```bash
   ./mvnw test
   ```
5. Submit a pull request with a detailed description of your changes.
