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
- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Loss of Spring Context in Tenant Resolver
  evidence: application.yaml changed tenant_identifier_resolver to a fully qualified class name, bypassing Spring context.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Security Vulnerability via URL Token Transmission
  evidence: OAuth2LoginSuccessHandler removed Cookie import, potentially exposing JWT.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Missing JWT Validation Error Handling
  evidence: JwtUtil removed JwtException catch, potentially leading to 500s instead of 401s.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Unrecoverable Profile Image Error State
  evidence: Header.tsx uses setImgError(true) but does not reset it if the profileImage prop changes.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Hardcoded Avatar Fallback
  evidence: Header.tsx renders a hardcoded "U" instead of a dynamic initial.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Non-Semantic DOM Structure
  evidence: Header.tsx includes an empty div for header-left just to satisfy CSS grid.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Tenant identifier resolver configuration lacks verification
  evidence: No tests exist to assert tenant context is actively resolved and applied.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Profile image referrer policy is unverified
  evidence: No component tests check that the img tag receives the referrerPolicy attribute.

- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-navbar-e-logout.md`
  summary: Application navigation layout composition is unverified
  evidence: No tests exist to verify that Navigation is properly rendered within Header.
- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: OAuth2LoginSuccessHandler Cookie import removed
  evidence: Bypassing standard cookie manipulation or HTTP-only setup for JWTs raises transmission security concerns.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: OAuth2LoginSuccessHandler missing audit logging
  evidence: Successful authentications do not update a last_login timestamp or record audit events.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: OAuth2LoginSuccessHandler missing error handling
  evidence: Missing logic for when OAuth2 providers return incomplete profiles without required claims.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: UsuarioRepository missing @Repository annotation
  evidence: Removal of this annotation disables automatic persistence exception translation.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: UsuarioRepository missing google_subject_id index
  evidence: findByGoogleSubjectId is on the critical login path, but there's no DB migration creating an index for it.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: JwtUtil JwtException import removed
  evidence: Suggests explicit handling of token validation errors like expiration or malformed signatures was removed.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: Tenant isolation missing tests
  evidence: multitenancy setup in application.yaml lacks test verification for cross-tenant data isolation.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: application.yaml OAuth2 hardcoded secrets risk
  evidence: security.oauth2.client block does not clearly use placeholder variables for secrets.

- source_spec: `_bmad-output/implementation-artifacts/spec-header-transparente-flutuante.md`
  summary: Header CSS hardcoded z-index
  evidence: z-index: 100 used without a centralized scale, risking overlaps with other components.

## Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
- Entire React application unmounts on route rendering error [frontend/src/App.tsx:68]

## Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)
- Missing Backend Logout Endpoint (belongs to Story 1.4)
- Incomplete CSRF Setup on Frontend
- Missing automated tests for auth flows
- Missing frontend interception of 401 errors during API access
- Concurrent OAuth logins for same new user could cause DataIntegrityViolationException

## Deferred from: code review of spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md (2026-08-18)
- Missing Automated Tests
- Lack of Asynchronous Thread Context Propagation

## Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
- Vazamento de contexto no TenantContext: Usando ThreadLocal padrão.
- Omissão de acessibilidade no dropdown do Header (Esc): Falta listener para a tecla Escape.
- Preenchimento extra no mobile para rodapé: O .main-content tem padding inferior de 80px herdado da antiga navegação.
- Ausência de testes de integração e endpoints backend: verification-gap acusou falta de testes para controllers e filtros criados.
- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-uso-tailwind.md`
  summary: Adicionar testes de navegação para certificar que rotas não-raiz renderizam o conteúdo esperado.
  evidence: Surfaced by verification-gap review; App.test.tsx apenas verifica a rota /.
- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-uso-tailwind.md`
  summary: Adicionar teste automatizado E2E ou de artefatos para verificar se os estilos do Tailwind são processados e aplicados com sucesso na build.
  evidence: Surfaced by verification-gap review; O projeto atual não valida o CSS final processado.
- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-uso-tailwind.md`
  summary: Corrigir a11y: adicionar aria-live e role="status" ao estado de carregamento e aria-controls ao botão de perfil.
  evidence: Surfaced by blind-hunter review.
- source_spec: `_bmad-output/implementation-artifacts/spec-correcao-uso-tailwind.md`
  summary: Utilizar a inicial do usuário logado no avatar padrão em vez de 'U' fixo.
  evidence: Surfaced by blind-hunter review.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
  summary: Adicionar testes de unidade e integração (Backend e Frontend) para Contas
  evidence: Nenhuma cobertura de teste foi adicionada na implementação da story 2.1.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
  summary: Adicionar métodos equals() e hashCode() na entidade Conta
  evidence: Conta não possui implementação customizada recomendada pelo JPA.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
  summary: Implementar Focus Trap acessível e completo no Modal
  evidence: Componente genérico Modal.tsx não possui restrição de foco para teclado.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
  summary: Adicionar suporte à paginação no backend e listagem de Contas
  evidence: O endpoint /api/contas retorna todos os registros de uma vez.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
  summary: Adicionar tratamento e exibição de erro da API com parsing estruturado no frontend
  evidence: useContas.ts lança erros genéricos sem extrair mensagens do body de erro.

## Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
- Endpoint criar Conta retorna 200 OK em vez de 201 Created.
- Modal genérico não possui Focus Trap acessível.
- Tratamento de erros de validação server-side não mapeados no formulário.
- Endpoint de listagem de contas não é paginado.
- Entidade Conta sem implementação customizada de equals() e hashCode().
- Modal não se adapta dinamicamente para Bottom Sheet em mobile.
- Ausência de testes verificando isolamento cross-tenant e invalidação de cache.
- Ausência de testes para contrato de violação de FK na deleção.
