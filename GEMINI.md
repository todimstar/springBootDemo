# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**TechForum** is a community forum system built with Spring Boot 3.x backend and Vue 3 frontend. This is a learning project designed for interview preparation, featuring user authentication, post management, commenting, and social features (likes, follows, notifications).

**Key Features:**
- JWT-based stateless authentication
- RESTful API design with Spring Security
- MyBatis for database operations
- Planned Redis caching and MinIO image storage
- Vue 3 + Element Plus frontend

## Architecture

### Project Structure

```
springBootDemo/
├── community-backgroundend/     # Spring Boot backend
│   ├── src/main/java/com/liu/springbootdemo/
│   │   ├── controller/          # REST API endpoints
│   │   ├── service/             # Business logic layer
│   │   ├── mapper/              # MyBatis data access
│   │   ├── entity/              # Domain models and DTOs
│   │   ├── config/              # Spring configurations
│   │   ├── filter/              # JWT authentication filter
│   │   ├── utils/               # Utility classes
│   │   └── exception/           # Custom exceptions and handler
│   └── src/main/resources/
│       ├── application.properties
│       └── mapper/              # MyBatis XML mappers (currently empty)
├── community-frontend/          # Vue 3 frontend
│   ├── src/
│   │   ├── views/               # Page components
│   │   ├── stores/              # Pinia state management
│   │   └── router/              # Vue Router configuration
│   └── vite.config.js           # Vite build configuration
└── background_details_doc/      # Comprehensive project documentation

IMPORTANT: Read `background_details_doc/项目文档阅读顺序指引.md` first for documentation overview.
```

### Backend Architecture (Spring Boot)

**Key Package Structure:**
- `controller`: REST endpoints for User, Post, Comment
- `service` + `service.impl`: Business logic implementation
- `mapper`: MyBatis interfaces (XML mappers not yet implemented)
- `entity`: Domain models (User, Post, Comment) with Lombok annotations
- `entity.DTO`: Data Transfer Objects (LoginInControllerDTO, LoginResponseDTO)
- `entity.VO`: View Objects (Result wrapper for API responses)
- `config`: SecurityConfig, EncoderConfig, WebConfig
- `filter`: JwtAuthenticationFilter for token validation
- `utils`: JwtUtil, SecurityUtil, ResponseUtil
- `exception`: GlobalExceptionHandler with custom exceptions

**Authentication Flow:**
1. User logs in via `/api/auth/login` → returns JWT token
2. Client includes token in `Authorization: Bearer <token>` header
3. JwtAuthenticationFilter validates token and sets SecurityContext
4. SecurityConfig controls access to endpoints (authenticated vs public)

**Database:**
- Database: `springboot_db` (MySQL 8.0+)
- Current tables: users, posts, comments (basic implementation)
- Planned tables: categories, likes, collects, follows, notifications, messages, reports, browsing_history
- See `background_details_doc/2.database-design.md` for complete schema

### Frontend Architecture (Vue 3)

**Tech Stack:**
- Vue 3 with Composition API
- Vite for build tooling
- Pinia for state management
- Vue Router for navigation
- Element Plus for UI components
- Axios for HTTP requests

**Key Routes:**
- `/` - Home page
- `/posts` - Post list view
- `/posts/:id` - Post detail with comments
- `/create-post` - Create new post (requires authentication)
- `/login` - User login

**API Proxy:** Frontend dev server proxies `/api` to `http://localhost:8080`

**Authentication:** Managed via Pinia store (`stores/auth.js`), router guards protect authenticated routes

## Common Development Commands

### Backend (Spring Boot)

**Build and Run:**
```bash
cd community-backgroundend
./mvnw clean install           # Build project
./mvnw spring-boot:run         # Run development server
```

**Run Tests:**
```bash
./mvnw test                    # Run all tests
./mvnw test -Dtest=ClassName   # Run specific test class
```

**Package for Production:**
```bash
./mvnw clean package           # Creates JAR in target/
java -jar target/springBootDemo-0.0.1-SNAPSHOT.jar
```

**Database Setup:**
1. Create database: `CREATE DATABASE springboot_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. Update credentials in `application.properties`
3. Run schema from `background_details_doc/2.database-design.md`

**API Documentation:**
- Knife4j available at: `http://localhost:8080/doc.html` (once configured)

### Frontend (Vue 3)

**Install and Run:**
```bash
cd community-frontend
npm install                    # Install dependencies
npm run dev                    # Start dev server (http://localhost:5173)
npm run build                  # Production build
npm run preview                # Preview production build
```

**Linting and Formatting:**
```bash
npm run lint                   # Run ESLint
npm run format                 # Format with Prettier
```

## Important Development Notes

### Backend Development

**Security Configuration:**
- JWT secret is hardcoded in JwtUtil - should use environment variables in production
- Token expiration: 1 hour (configurable)
- SecurityConfig: `/api/auth/**` is public, other `/api/**` requires authentication
- CORS configured in WebConfig for `http://localhost:5173`

**MyBatis Configuration:**
- XML mapper location: `classpath:mapper/*.xml`
- Type aliases package: `com.liu.springbootdemo.entity`
- Camel case mapping enabled: database `create_time` → Java `createTime`
- **Note:** No XML mappers exist yet - MyBatis is configured but not actively used

**Exception Handling:**
- GlobalExceptionHandler centralizes all exception handling
- Custom exceptions: InvalidInputException, UnauthorizedException, NotFindException, UserAlreadyExistsException, NotAuthorException, BizException
- All responses wrapped in Result<T> object

**Entity Relationships:**
- User → Posts (1:N)
- User → Comments (1:N)
- Post → Comments (1:N)
- Foreign keys defined in entity classes but relationships not fully implemented yet

### Frontend Development

**Pinia Stores:**
- `auth.js`: Manages authentication state and token storage
- Token stored in localStorage, auto-loaded on app init

**Router Guards:**
- Routes requiring authentication: `/create-post`
- Unauthenticated users redirected to `/login`
- Guard implemented in `router/index.js` beforeEach hook

**API Communication:**
- Axios configured with `/api` base path (proxied to backend)
- Token automatically included in Authorization header (if implemented in axios config)

### Project Documentation

**Essential Documentation Files (in `background_details_doc/`):**
1. `项目文档阅读顺序指引.md` - **READ THIS FIRST** - Documentation navigation guide
2. `1.TechForum.project-navigation.md` - Project roadmap and progress tracking
3. `2.database-design.md` - Complete database schema with SQL scripts
4. `3.API接口文档.api-documentation.md` - API endpoint specifications
5. `4.development-plan.md` - Phased development plan (10 weeks)
6. `5.tech-stack-doc.md` - Technology stack decisions and configuration
7. `6-10.*.html` - UI prototypes for reference

**Development Phases:**
- Phase 1: Database design, validation, categories (Week 1-2)
- Phase 2: Image storage (MinIO), interactions (likes, follows) (Week 3-4)
- Phase 3: Notifications, search, messaging (Week 5-6)
- Phase 4: Admin dashboard (Week 7)
- Phase 5: Optimization, caching, deployment (Week 8-10)

### Code Style and Patterns

**Backend Conventions:**
- Use Lombok annotations: `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`
- Service layer uses interface + implementation pattern
- Return `Result<T>` wrapper for all API responses
- Use `@Valid` for request body validation (when implemented)
- Exceptions throw custom exception types, caught by GlobalExceptionHandler

**Frontend Conventions:**
- Vue 3 Composition API preferred
- Use `<script setup>` syntax
- Component names use PascalCase (e.g., `PostListView.vue`)
- Lazy loading for route components (except home)

## Technology Stack

### Backend
- Spring Boot 3.5.5
- Spring Security 6.x with JWT (jjwt 0.12.6)
- MyBatis 3.0.3 (configured, minimal usage)
- MySQL 8.0+ (mysql-connector-java 8.0.33)
- Lombok 1.18.20
- Knife4j 4.3.0 (OpenAPI/Swagger)

**Planned:** Redis, MinIO, Validation API

### Frontend
- Vue 3.5.18
- Vite 7.0.6
- Vue Router 4.5.1
- Pinia 3.0.3
- Element Plus 2.11.3
- Axios 1.12.2
- ESLint + Prettier

**Node Version:** 20.19.0 or >= 22.12.0

## Git Workflow

**Current Branch:** `main`

**Recent Work:**
- Database and API documentation finalization
- Frontend/backend project structure setup
- File reorganization (renamed Chinese documentation files)

**Do not commit:** `.env` files, `target/`, `node_modules/`, IDE-specific files

## Known Issues and TODOs

### Technical Debt
1. MyBatis XML mappers not created - using annotations or repositories instead
2. Deep pagination performance issue (requires cursor-based pagination)
3. No rate limiting on API endpoints
4. Password reset functionality missing
5. Email verification not implemented
6. Redis caching not yet integrated
7. Image upload validation and security missing
8. Comment threading (nested replies) not fully implemented

### Planned Features
- Redis caching layer for hot posts and user data
- MinIO for distributed image storage
- Categories/sections for organizing posts
- Likes, favorites, follows functionality
- Real-time notifications system
- Private messaging
- Admin dashboard for moderation
- Full-text search (MySQL LIKE initially, Elasticsearch later)
- Docker Compose deployment setup

## Additional Resources

**Official Documentation:**
- Spring Boot: https://spring.io/projects/spring-boot
- MyBatis: https://mybatis.org/mybatis-3/zh/index.html
- Vue 3: https://vuejs.org/
- Element Plus: https://element-plus.org/

**Recommended Tools:**
- API Testing: Apifox, Postman, or Knife4j UI
- Database: Navicat, DataGrip
- Diagrams: draw.io, dbdiagram.io

## Interview Preparation Focus

This project demonstrates:
1. JWT stateless authentication and Spring Security filter chain
2. RESTful API design principles
3. Frontend-backend separation architecture
4. Exception handling and validation best practices
5. Modern frontend development with Vue 3 Composition API

**Key talking points:**
- Authentication flow and security mechanisms
- Database design decisions and indexing strategies
- Redis caching strategies (once implemented)
- Performance optimization approaches
- Full-stack development workflow

## 项目记忆

- 想要修改的代码都放在md文档里不要直接操作我的项目文件，如果关于项目文件结构的改变也只需要创建占位空文件。因为我要手打代码以便学到知识
- 符合现代开发规范的异常处理
- 可以多发挥主观能动性，积极寻找有关文件和用法
- 不要问题解决方案复杂化，只要求你的知识要专业性现代性并能让我学到东西
- 设计思想和代码符合现代开发规范，并讲明选用理由和场景，可以像书一样深入浅出