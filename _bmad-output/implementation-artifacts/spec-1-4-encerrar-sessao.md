---
title: 'Story 1.4: Encerrar Sessão (Logout) e Limpeza de Estado'
type: 'feature'
created: '2026-08-18'
status: 'done'
baseline_commit: '492c5b1bd23e8a9c3ce9ab35ec3dde62ad156377'
review_loop_iteration: 0
context: ['_bmad-output/implementation-artifacts/epic-1-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O usuário autenticado não tem a opção de encerrar sua sessão e apagar os dados cacheados, podendo expor suas informações financeiras ao deixar o dispositivo.

**Approach:** Criar um endpoint `/api/auth/logout` no backend para limpar e invalidar o cookie JWT (`AUREUS_SESSION`). No frontend, adicionar um botão de "Sair" na navegação que chama esse endpoint, limpa o estado global de autenticação (Zustand), limpa o cache de dados (React Query) e redireciona o usuário para a tela de login.

## Boundaries & Constraints

**Always:**
- O endpoint de logout no backend deve expirar o cookie usando o mesmo nome e path da criação (`AUREUS_SESSION`, `/`), definindo `Max-Age=0`, `HttpOnly`, e `SameSite=Lax`.
- O frontend deve limpar tanto o estado global do Zustand (`useAuthStore().logout()`) quanto os dados cacheados do React Query (`queryClient.clear()`).

**Ask First:**
- Modificar a identidade visual ou componentes de interface além da adição do botão Sair na Navegação.

**Never:**
- Manter dados em cache na memória do cliente após a ação de logout.
- Modificar o fluxo de Auth nativo do Google, visto que o logout encerra apenas a sessão na aplicação Aureus.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Logout de Sessão Ativa | Usuário clica no botão "Sair" na navegação | Chama a API, cookie expirado, cache limpo, estado auth zerado, redireciona Login | N/A |
| Logout com Falha de Rede | Clicar "Sair" mas a chamada API `/api/auth/logout` falha | Ainda limpa Zustand e QueryClient localmente e redireciona para Login | Toast amigável no catch, com fallback de limpeza local |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java` -- Adicionar endpoint POST `/logout` para limpar o cookie de sessão retornando um `ResponseCookie` ou setando o header `Set-Cookie`.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java` -- Assegurar que `/api/auth/logout` esteja acessível para usuários autenticados (ou permitir all) e que não crie fricções desnecessárias com CSRF (o token CSRF vai no cookie).
- `frontend/src/components/Navigation/Navigation.tsx` -- Adicionar o botão "Sair" e implementar `handleLogout()`. Executar o fetch POST para a API, invocar o React Query `queryClient.clear()`, invocar o `logout()` do authStore, e navegar para `/login`.

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java` -- Criar endpoint `POST /logout` que retorna `ResponseEntity` configurado com um header `Set-Cookie` para expirar e deletar o cookie `AUREUS_SESSION` (Max-Age=0).
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java` -- Garantir acesso livre ou autenticado no `.requestMatchers("/api/auth/logout").permitAll()` para não gerar bloqueios se o token expirar no meio do request.
- [x] `frontend/src/components/Navigation/Navigation.tsx` -- Adicionar botão/ícone "Sair". Na função associada ao clique, usar `fetch('/api/auth/logout', { method: 'POST' })` e, em seguida ou independente de erro de rede, invocar `queryClient.clear()`, `useAuthStore.getState().logout()`, e por fim redirecionar para `/login`.

**Acceptance Criteria:**
- Given que o usuário está autenticado
- When ele aciona o comando "Sair" na interface
- Then o backend invalida o Cookie de sessão e o frontend limpa todos os dados em cache (Zustand e React Query)
- And o usuário é redirecionado para a tela de login inicial sem retenção de estado anterior

## Verification

**Commands:**
- `cd backend && ./mvnw test` -- expected: O contexto do Spring Boot sobe corretamente.
- `cd frontend && npm run build` -- expected: Compilação TypeScript do Vite conclui sem erros no novo layout de Navegação.

## Suggested Review Order

**Backend Logout Logic**

- Endpoint POST /logout invalida o cookie AUREUS_SESSION.
  [`AuthController.java:26`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java#L26)

- Liberação do endpoint no Spring Security para não gerar 403 por token expirado.
  [`SecurityConfig.java:28`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java#L28)

**Frontend Logout Flow**

- Botão Sair e `handleLogout` limpam Zustand e React Query, enviando header CSRF.
  [`Navigation.tsx:13`](../../frontend/src/components/Navigation/Navigation.tsx#L13)

- Estilização extraída para não poluir o componente com CSS inline.
  [`Navigation.css:1`](../../frontend/src/components/Navigation/Navigation.css#L1)

**Configurações de Bibliotecas Frontend**

- Provedor do React Query configurado na inicialização.
  [`main.tsx:11`](../../frontend/src/main.tsx#L11)

- Injeção do `<Toaster />` global no React Hot Toast.
  [`App.tsx:64`](../../frontend/src/App.tsx#L64)

### Review Findings
- [x] [Review][Patch] Exposição de Endpoints do Actuator — Dependência actuator inserida mas `SecurityConfig.java` expõe todos os endpoints não mapeados.
- [x] [Review][Patch] Decodificação Incompatível do JWT Secret — `JwtUtil.java` usa Base64 decode mas o secret default no `application.yaml` é Hexadecimal.
- [x] [Review][Patch] Atributo Secure inconsistente no Cookie de Logout — `AuthController.java` tem `.secure(false)` fixo, invalidando em HTTPS.
- [x] [Review][Patch] Atributos omitidos na limpeza de Cookie JWT inválido — Falta `.sameSite("Lax")` e `.secure(true)` no `JwtAuthenticationFilter.java` ao resetar cookie.
- [x] [Review][Patch] Chave YAML inválida para Hibernate — `[tenant_identifier_resolver]` deveria ser `tenant_identifier_resolver` no `application.yaml`.
- [x] [Review][Patch] Falha no teste do App.test.tsx — Faltam o `QueryClientProvider` e métodos mockados do `useAuthStore` (profileImage, logout).
- [x] [Review][Patch] Estado de erro não tratado no catch do App.tsx — Erros de rede não limpam o estado da loja caso seja diferente de 'Não autorizado'.
- [x] [Review][Defer] Vazamento de contexto no TenantContext — Usando ThreadLocal padrão. — deferred, pre-existing
- [x] [Review][Defer] Omissão de acessibilidade no dropdown do Header (Esc) — Falta listener para a tecla Escape. — deferred, pre-existing
- [x] [Review][Defer] Preenchimento extra no mobile para rodapé — O .main-content tem padding inferior de 80px herdado da antiga navegação. — deferred, pre-existing
- [x] [Review][Defer] Ausência de testes de integração e endpoints backend — verification-gap acusou falta de testes para controllers e filtros criados. — deferred, pre-existing
