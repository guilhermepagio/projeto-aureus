---
status: final
updated: 2026-08-19
---
# Architecture Spine: Aureus V1

## Paradigm
Modular Monolith (Backend) + Responsive Single Page Application (Frontend)

## Stack
- **Frontend:** React (SPA)
- **Backend:** Java Spring Boot
- **Database:** PostgreSQL
- **Migrations:** Flyway
- **Styling:** Tailwind CSS v4

## Inherited Constraints
- **C-1:** Architecture serves as the technical contract for developers and AI agents implementing the Aureus V1 product.

## Invariants

### System & Boundaries
- **AD-1: Communication Paradigm**
  - **Binds:** How the frontend consumes backend data.
  - **Prevents:** Mixing REST with GraphQL or tight coupling via server-side rendering (SSR/Thymeleaf).
  - **Rule:** Strict REST API (JSON over HTTP). Frontend is decoupled and consumes endpoints.
- **AD-2: Backend Modularity**
  - **Binds:** How Spring Boot code is structured.
  - **Prevents:** A tangled monolith ("big ball of mud") or premature microservices.
  - **Rule:** Modular Monolith. The codebase remains a single deployable `.jar` but is strictly divided by domain packages (e.g., `finance`, `users`, `infra/security`).

### Data & State
- **AD-3: Data Isolation (Multi-Tenancy)**
  - **Binds:** How user data is separated in the database.
  - **Prevents:** Cross-user data leakage and complex multi-database setups.
  - **Rule:** Logical isolation. Every core domain entity (Account, Category, Expense) MUST have a `usuario_id` column. All repository queries MUST enforce a filter by the authenticated token's user ID.
- **AD-4: Database Versioning**
  - **Binds:** How schema changes are applied.
  - **Prevents:** Automatic, unpredictable schema generation in production (e.g., `hibernate.hbm2ddl.auto=update`).
  - **Rule:** `[ADOPTED]` Flyway. All schema mutations must be written as explicit `V1__init.sql` migration scripts.

### Frontend Patterns
- **AD-5: Global State Management**
  - **Binds:** How shared state (e.g., the globally selected "Month") is managed across tabs.
  - **Prevents:** Over-engineering with Redux or prop-drilling chaos.
  - **Rule:** Zustand. Used exclusively for cross-tab context; local UI state remains in `useState`.
- **AD-6: Data Fetching and Caching**
  - **Binds:** How REST calls are executed and cached in React.
  - **Prevents:** Manual `useEffect` implementations that reinvent loading/error states and race conditions.
  - **Rule:** React Query (TanStack Query) paired with Axios/Fetch.

### Security
- **AD-7: Authentication Token Storage**
  - **Binds:** Where the JWT token resides on the client.
  - **Prevents:** XSS vulnerabilities associated with `localStorage`.
  - **Rule:** HttpOnly Cookie. The backend MUST issue the JWT in a secure, HttpOnly cookie that the frontend cannot access via JavaScript, but the browser automatically attaches to subsequent requests.
- **AD-8: Dual Authentication Flow (Google One Tap + OAuth 2.0)**
  - **Binds:** How Google login is implemented across Frontend and Backend.
  - **Prevents:** Irresponsible replacement of a secure fallback flow, or vulnerable manual JWT validation.
  - **Rule:** The system MUST support Google One Tap via a native prompt (no redirect), sending the credential via POST to a dedicated backend endpoint. The backend MUST validate the JWT securely using `google-api-client` (`GoogleIdTokenVerifier`). The system MUST ALSO maintain the classic OAuth 2.0 flow (Spring Security `.oauth2Login()`) as a fallback in case the native prompt is blocked.
