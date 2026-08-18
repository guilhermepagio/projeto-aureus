- source_spec: `none`
  summary: Regenerate aureus-erd.png
  evidence: The static image is out of sync with the updated DBML after adding OAuth2 fields.
- source_spec: `none`
  summary: Add session/refresh token entity to data and domain models
  evidence: Required for full OAuth2 implementation, but out of scope for the immediate structural sync.
- source_spec: `none`
  summary: Add audit attributes to domain classes (Conta, Categoria, Despesa, Receita)
  evidence: These classes are missing criadoEm/atualizadoEm which exist in the DBML.
- source_spec: `none`
  summary: Sync Despesa attributes between class diagram and DBML
  evidence: Class diagram is missing localCompra, dataCompra, and observacoes that are defined in DBML.
- source_spec: `none`
  summary: Sync Receita attributes between class diagram and DBML
  evidence: Class diagram is missing observacoes defined in DBML.
- source_spec: `none`
  summary: Resolve structural discrepancy for TipoMovimento
  evidence: DBML stores fixed/variable directly on transactions, while class diagram abstracts it via ContextoFinanceiro.
- source_spec: none
  summary: Story 1.2 - Autenticação via Google (OAuth 2.0) e Tratamento de Erros
  evidence: Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
- source_spec: none
  summary: Story 1.3 - Proteção de Sessão e Isolamento de Dados
  evidence: Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
- source_spec: none
  summary: Story 1.4 - Encerrar Sessão (Logout) e Limpeza de Estado
  evidence: Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-esqueleto-visual-barra-navegacao.md`
  summary: Add Navigation icons
  evidence: The UX design requires a bottom navigation bar with 5 icons, but the current implementation lacks icons.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-esqueleto-visual-barra-navegacao.md`
  summary: Add semantic states colors to CSS tokens
  evidence: Essential color tokens such as text colors, secondary backgrounds, and semantic states (success/error) are missing.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-1-esqueleto-visual-barra-navegacao.md`
  summary: Add automated tests for client-side routing
  evidence: Unverified navigation link paths and client-side routing paths without component integration tests.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-encerrar-sessao.md`
  summary: Fix permissive authorization fallback and protect Actuator endpoints
  evidence: SecurityConfig currently uses `.anyRequest().permitAll()`, which inadvertently exposes unmatched endpoints, including Spring Boot Actuator, without authentication.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-encerrar-sessao.md`
  summary: Implement backend unit tests for AuthController
  evidence: Review caught that the logout endpoint lacks test coverage.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-encerrar-sessao.md`
  summary: Implement frontend unit tests for Navigation component
  evidence: Review caught that the handleLogout logic in the frontend lacks test coverage.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-encerrar-sessao.md`
  summary: Configure global defaults for React QueryClient
  evidence: The QueryClient in main.tsx uses out-of-the-box settings, missing optimized defaults like staleTime and refetchOnWindowFocus.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-encerrar-sessao.md`
  summary: Configure secure attribute for session cookie based on environment
  evidence: AuthController hardcodes secure(false) for the AUREUS_SESSION cookie, which must be enabled dynamically for production.

## Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- Interface de carregamento (Loading) não estlizada [frontend/src/App.tsx]
- Redirecionamento de login não preserva o estado de rota prévia (`location.state`) [frontend/src/App.tsx:47]
- Rotas hardcoded ao invés de usar constantes centralizadas [frontend/src/App.tsx]
- Ausência de Error Boundary genérico para falhas do React [frontend/src/App.tsx]
- Ausência de testes end-to-end e componentes para o Auth Fetch e Logout
- Ausência de verificação contra bypass de rota protegida
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-correcao-botao-sair.md`
  summary: Missing verification for Google profile picture extraction
  evidence: No assertion checks that the picture attribute from OAuth2User is correctly mapped and saved to the user entity.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-correcao-botao-sair.md`
  summary: Missing verification for profile picture inclusion in /me response
  evidence: No test covers this endpoint or its consumption in the frontend.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-correcao-botao-sair.md`
  summary: Missing verification for Header logout and profile UI
  evidence: No frontend test checks that Header displays the user profile image or that logout triggers the flow.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-correcao-botao-sair.md`
  summary: Unnecessary Database Writes on Login
  evidence: OAuth2LoginSuccessHandler saves the user entity unconditionally on every login.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-correcao-botao-sair.md`
  summary: Inefficient Profile Fetching
  evidence: AuthController.me() queries the database on every check.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-correcao-botao-sair.md`
  summary: Direct Repository Access in AuthController
  evidence: Bypasses the service layer.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-4-correcao-botao-sair.md`
  summary: Missing Strongly Typed DTO in AuthController
  evidence: /api/auth/me endpoint returns a loosely typed Map<String, Object>.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
  summary: Hardcoded frontend redirect URI in OAuth2LoginSuccessHandler.
  evidence: The URL `http://localhost:5173/` is hardcoded instead of being loaded from application properties.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
  summary: Overly broad exception catching in JwtAuthenticationFilter.
  evidence: The filter catches generic `Exception` instead of specific JWT exceptions, potentially hiding system errors.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
  summary: Empty Response Body on 401 Unauthorized in JwtAuthenticationFilter.
  evidence: The filter sets status 401 but writes no JSON body, making frontend error handling harder.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
  summary: Potential Token and Cookie Expiration Mismatch.
  evidence: The `AUREUS_SESSION` cookie uses a hardcoded 86400 maxAge which might drift from the JWT expiration property.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
  summary: Missing verification for Tenant context population.
  evidence: There are no unit or integration tests verifying that `JwtAuthenticationFilter` populates and clears the `TenantContext`.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
  summary: Missing verification for Session cookie SameSite security attribute.
  evidence: There are no tests verifying that `OAuth2LoginSuccessHandler` emits a cookie with `SameSite=Lax`.
- source_spec: `_bmad-output/implementation-artifacts/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
  summary: Missing verification for Hibernate tenant resolution.
  evidence: There are no tests verifying that `CurrentTenantIdentifierResolverImpl` correctly returns the value from `TenantContext`.
