---
title: 'Story 1.2: Autenticação via Google (OAuth 2.0) e Tratamento de Erros'
type: 'feature'
created: '2026-08-17'
status: 'done'
baseline_commit: 'dd04e0730e15d66f72e57ae0c2d71c3b13cd7656'
review_loop_iteration: 1
context: ['_bmad-output/implementation-artifacts/epic-1-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** A aplicação não possui um sistema de autenticação, impedindo o isolamento seguro dos dados dos usuários e o rastreamento das suas respectivas informações financeiras.

**Approach:** Implementar autenticação centralizada usando Google OAuth 2.0 no backend (Spring Boot), gerando um token JWT próprio que será trafegado exclusivamente via cookies `HttpOnly`. No frontend, criar uma tela de login minimalista, gerenciar o estado global de autenticação com Zustand, e tratar falhas de login (ex: cancelamento, erro de rede) exibindo um Toast amigável.

## Boundaries & Constraints

**Always:**
- Utilizar exclusivamente Google OAuth 2.0 (OpenID Connect) solicitando os escopos `openid`, `profile` e `email`.
- Na primeira autenticação, criar o registro do usuário no banco local contendo o Google Subject ID. Em logins subsequentes, identificar a conta por esse mesmo ID.
- Armazenar o Token JWT emitido pela API exclusivamente em um Cookie `HttpOnly` com `SameSite=Lax`.
- O frontend deve interceptar falhas 401 e requerer um novo login.

**Ask First:**
- Mudar a estratégia de persistência ou a biblioteca de geração do JWT no backend.
- Alterar o design ou fluxo da tela de login previamente especificado.

**Never:**
- Armazenar Tokens JWT no `localStorage`, `sessionStorage` ou expor no corpo da resposta HTTP.
- Criar formulários de login tradicionais (usuário/senha).
- Utilizar Tailwind CSS (usar Vanilla CSS conforme projeto).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Novo Usuário Logando | Usuário clica em Login com Google | Cria registro na base e gera JWT Cookie | N/A |
| Usuário Existente Logando | Usuário clica em Login com Google | Identifica pelo Subject ID, gera JWT Cookie | N/A |
| Rejeição de Autorização | Usuário cancela o OAuth | Frontend recebe erro e exibe Toast | Retorna à tela de login com Toast "Autorização cancelada" |
| Sessão Expirada / 401 | JWT expirado e tenta acessar API | Ocorre 401, frontend limpa estado e redireciona | Redireciona para o login |

</frozen-after-approval>

## Code Map

- `backend/pom.xml` -- Requer adicionar dependências para Spring Security, OAuth2 Client, Data JPA e JWT.
- `backend/src/main/resources/application.yaml` -- Configuração do client-id/client-secret (usando var de ambiente) e datasource.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java` -- Entidade base de usuário para persistência do Subject ID do Google.
- `frontend/package.json` -- Requer adicionar Zustand e pacote leve para toasty notifications se necessário.
- `frontend/src/App.tsx` -- Gerenciamento das rotas públicas (Login) vs privadas (Navegação baseada em aba).
- `frontend/src/components/Login/Login.tsx` -- Nova tela de login com botão Google.

## Tasks & Acceptance

**Execution:**
- [x] `backend/pom.xml` -- Adicionar dependências: `spring-boot-starter-security`, `spring-boot-starter-oauth2-client`, `spring-boot-starter-data-jpa`, e `jjwt` (ou lib equivalente).
- [x] `backend/src/main/resources/application.yaml` -- Configurar datasource PostgreSQL e Google OAuth2 properties.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java` -- Criar entidade. Adicionar constraints `@Column(unique = true, nullable = false)` ao `googleSubjectId`.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/...` -- Implementar `OAuth2LoginSuccessHandler` E `OAuth2LoginFailureHandler` (redireciona para `/login?error=true`).
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java` -- Configurar rotas, mapear `authorizationEndpoint.baseUri("/api/oauth2/authorization")`, e configurar proteção CSRF (`CookieCsrfTokenRepository.withHttpOnlyFalse()`).
- [x] `frontend/vite.config.ts` -- Adicionar proxy para rotas `/api` apontando para `http://localhost:8080`.
- [x] `frontend/package.json` -- Instalar zustand e react-hot-toast.
- [x] `frontend/src/store/authStore.ts` -- Criar store Zustand.
- [x] `frontend/src/components/Login/Login.tsx` -- Criar tela de Login, garantindo o uso de variáveis CSS oficiais da marca (sem colors hardcoded) e checando se já está logado.
- [x] `frontend/src/App.tsx` -- Proteger rotas (inclusive a curinga `*`) com timeout no fetch de `/api/auth/me`.

**Acceptance Criteria:**
- Given um usuário não autenticado, when ele acessa a raiz do app, then ele é redirecionado para a tela de login.
- Given a tela de login, when o usuário clica em Entrar com Google e conclui o fluxo OAuth, then o backend emite um JWT via cookie `HttpOnly` e o usuário visualiza o "shell" e navegação da aplicação.
- Given o retorno falho do OAuth, when redirecionado para o front, then um Toast informa que a autenticação falhou e mantém o usuário no Login.

## Spec Change Log

- **2026-08-17 - Loopback 1 (bad_spec)**: 
  - *Triggering finding:* Faltou proteção CSRF, proxy no Vite, mapeamento explícito do prefixo `/api` para OAuth e o Handler de falhas.
  - *Amended:* `Tasks & Acceptance` reescrito para incluir configuração de CSRF, proxy, `OAuth2FailureHandler` e restrições na base de dados.
  - *Known-bad state avoided:* Sessões inseguras contra CSRF e CORS failures durante dev, rotas 404 desprotegidas no frontend.
  - *KEEP Instructions:*
    - **KEEP** a estrutura da entidade `Usuario`, apenas adicione as *constraints*.
    - **KEEP** a lógica de geração/verificação no `JwtUtil` e `JwtAuthenticationFilter`, mas **REMOVA** a anotação `@Component` do filtro (para não registrar duplamente globalmente) e não engula exceções silenciosamente.
    - **KEEP** o layout do `Login.tsx` da iteração anterior, mas use as variáveis CSS do `index.css` (`var(--color-primary)`, etc) em vez de cores inline hex e verifique se o usuário já está logado.
    - **KEEP** a estrutura do `App.tsx` que construímos, mas garanta que a rota `*` (404) seja protegida e coloque um AbortSignal timeout no fetch de autenticação.

## Design Notes

A tela de Login deve ser minimalista, contendo a marca "Aureus" e o botão "Entrar com Google". Utilizar os tokens de cor da marca já definidos em `index.css`.
A integração OAuth2 no backend utilizará a configuração auto-mágica do Spring Boot + um custom SuccessHandler.
O `JWT` precisa usar uma secret estrita. O Cookie deve ter nome consistente, ex: `AUREUS_SESSION`.

## Verification

**Commands:**
- `cd backend && ./mvnw test` -- expected: Os contextos sobem e não há problemas de classpath.
- `cd frontend && npm run lint` -- expected: O lint passa no novo componente de login.
- `cd frontend && npm run build` -- expected: Compilação Typescript com sucesso.

## Suggested Review Order

**Segurança e Endpoints (Backend)**

- Central de configuração de segurança, habilitando OAuth2 e filtro JWT, com proteção CSRF.
  [`SecurityConfig.java:23`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java#L23)

- Criação do usuário na base de dados ao logar com Google e geração do cookie `AUREUS_SESSION`.
  [`OAuth2LoginSuccessHandler.java:25`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java#L25)

- Falha no OAuth2 redirecionando para a tela de login com query param de erro.
  [`OAuth2LoginFailureHandler.java:17`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginFailureHandler.java#L17)

- Validação da requisição e extração do JWT a partir do cookie para o contexto de segurança.
  [`JwtAuthenticationFilter.java:25`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java#L25)

- Endpoint para o frontend verificar se o cookie JWT atual é válido.
  [`AuthController.java:18`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java#L18)

- Geração e extração dos claims do token JWT.
  [`JwtUtil.java:23`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtUtil.java#L23)

- Entidade de usuário mapeando as credenciais persistentes da conta Google.
  [`Usuario.java:13`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java#L13)

**Proteção de Rotas e UI (Frontend)**

- Gerenciador de estado para autenticação, controlando loading e identificador do sujeito.
  [`authStore.ts:9`](../../frontend/src/store/authStore.ts#L9)

- Container principal protegendo rotas e inicializando o estado de sessão com timeout.
  [`App.tsx:28`](../../frontend/src/App.tsx#L28)

- Tela de autenticação com tratamento de mensagens de erro e redirecionamento de usuário logado.
  [`Login.tsx:6`](../../frontend/src/components/Login/Login.tsx#L6)

- Proxy de requisições de `/api` para o backend para contornar problemas de CORS.
  [`vite.config.ts:8`](../../frontend/vite.config.ts#L8)


### Review Findings
- [x] [Review][Patch] Filter Chain Blocked on Expired Token: JwtAuthenticationFilter returns 401 instead of delegating [backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java]
- [x] [Review][Patch] Hardcoded Post-Login Redirect URL: redirects to localhost:5173 [backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java]
- [x] [Review][Patch] Desynchronized Cookie and JWT Expirations: maxAge hardcoded [backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java]
- [x] [Review][Patch] Missing Secure Flag for Authentication Cookie [backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java]
- [x] [Review][Patch] Suboptimal JWT Secret Key Parsing [backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtUtil.java]
- [x] [Review][Patch] Valid session wiped locally on transient errors [frontend/src/App.tsx:561-567]
- [x] [Review][Defer] Missing Backend Logout Endpoint — deferred, pre-existing (belongs to Story 1.4)
- [x] [Review][Defer] Incomplete CSRF Setup on Frontend — deferred, pre-existing (to be handled later)
- [x] [Review][Defer] Missing automated tests for auth flows — deferred, pre-existing
- [x] [Review][Defer] Missing frontend interception of 401 errors during API access — deferred, pre-existing
- [x] [Review][Defer] Concurrent OAuth logins for same new user could cause DataIntegrityViolationException — deferred, pre-existing