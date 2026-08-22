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

## Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- source_spec: `_bmad-output/implementation-artifacts/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
  summary: Endpoint criar Categoria retorna 200 OK em vez de 201 Created.
  evidence: Pre-existing pattern from Conta Controller; low impact for now.
- source_spec: `_bmad-output/implementation-artifacts/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
  summary: Entidade Categoria sem métodos equals() e hashCode().
  evidence: Pre-existing pattern; low impact for current detached entity usage.
- source_spec: `_bmad-output/implementation-artifacts/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
  summary: O endpoint de exclusão assume 400 apenas como FK violation.
  evidence: Out of scope to overhaul error parsing; validation details might be masked.
- source_spec: `_bmad-output/implementation-artifacts/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
  summary: Listagem de Categorias não possui paginação.
  evidence: Frontend and backend fetch all records at once, risky for large tenants.
- source_spec: `_bmad-output/implementation-artifacts/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
  summary: Falta de tratamento de timeout para requisições frontend.
  evidence: Network hangs indefinitely instead of aborting.
- source_spec: `_bmad-output/implementation-artifacts/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
  summary: Testes automatizados ausentes para Categoria.
  evidence: Multi-tenant isolation, FK constraint errors, and frontend cache invalidation have no automated tests.

## Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
- Missing Location header in Categoria creation.
- useCategorias.ts lacks AbortSignal.
- Optimistic locking (missing @Version).
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
  summary: Improve UX by checking both accounts and categories simultaneously to avoid a "waterfall" of empty states.
  evidence: A user missing both will first see the warning for accounts, fix it, and then unexpectedly face a second block for categories.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
  summary: Add data-testid attributes to loading, error, and empty states.
  evidence: Automated testing will be brittle without semantic data-testid attributes.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
  summary: Add a Retry button to the error state.
  evidence: Currently, the user is forced to manually refresh the entire application if the initial fetch fails.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
  summary: Improve error state UI to match the polished, card-based layouts of the empty states.
  evidence: The error state UI is a simple line of text, which feels visually inconsistent.
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
  summary: Add automated tests to verify dependency guards block access to transaction routes.
  evidence: The test suite would not fail if a developer removed the RequiresDependencies wrapper, allowing users to access transaction pages without required data.

## Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
- Missing `role="status"` on loading state.
- Missing `role="alert"` on error state.
- Missing `data-testid` attributes on conditional UI states.
- Repetitive `<ProtectedRoute><RequiresDependencies>` wrappers in App.tsx.
- Scope creep / missing `aria-hidden="true"`: Unrequested additions of `lucide-react` icons in Header.tsx.
- source_spec: `_bmad-output/implementation-artifacts/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
  summary: Missing Relationship Ownership Validation (verify if related conta/categoria belong to user).
  evidence: Not specific to this story, applies globally to existing entities too.
- source_spec: `_bmad-output/implementation-artifacts/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
  summary: Missing Pagination for /api/despesas-fixas and /api/receitas-fixas.
  evidence: Table grows large over time, missing in other controllers as well.
- source_spec: `_bmad-output/implementation-artifacts/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
  summary: Clarify if fixed items need an end date (dataFim) or active flag to prevent history mutation on deletion.
  evidence: Currently deletion removes the item entirely, which might break historical monthly projections. Needs product definition.
- source_spec: `_bmad-output/implementation-artifacts/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
  summary: Missing backend integration tests for new endpoints.
  evidence: Project currently lacks test infrastructure, deferring per Epic 1 retro items.
- source_spec: `_bmad-output/implementation-artifacts/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
  summary: Missing frontend component tests for DespesasFixasPage and ReceitasFixasPage.
  evidence: Project currently lacks test infrastructure, deferring per Epic 1 retro items.

## Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
- Falta de validação de propriedade (Tenant) nos relacionamentos informados (conta/categoria)
- Mutação de histórico financeiro em exclusões (falta de dataFim/soft-delete)
- Falta de paginação nos endpoints `GET` de listagem
- Falta de cobertura de testes automatizados (integração e UI)
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Controllers return JPA Entities directly instead of using DTOs, risking Mass Assignment
  evidence: Found in DespesaVariavelController and ReceitaVariavelController (Pre-existing project pattern)
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Duplicated date logic (preencherDataFim) in controllers instead of centralizing in service or lifecycle hook
  evidence: Found repeated in both variable controllers
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Missing DB indexes for dataInicio and dataFim which will be heavily queried in Epic 4
  evidence: Not explicitly requested in the spec, but necessary for future performance
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Missing class-level validation to ensure dataFim >= dataInicio
  evidence: Although the controller calculates it, programmatic API changes could introduce inconsistencies
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Unbounded findAll queries in backend list endpoints
  evidence: Pre-existing technical debt across the project (Epic 2, etc.)
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: DRY violation in frontend form/list pages for variable expenses and revenues
  evidence: Boilerplate is duplicated between the two modules
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Missing Auditing fields (createdAt, updatedAt) across entities
  evidence: Technical debt from prior architecture setup
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Lack of global @ControllerAdvice for standard constraint violations
  evidence: Pre-existing pattern where controllers individually catch DataIntegrityViolationException
- source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
  summary: Concurrent deletion causes unhandled 500 EmptyResultDataAccessException
  evidence: Pre-existing basic CRUD pattern does not handle race conditions gracefully

## Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
- Missing automated verification tests (dataFim, relational errors, API parsing)
- Missing DTOs, Service Layer, and Global Exception Handler
- Fetcher swallows non-JSON 500 errors
- source_spec: `_bmad-output/implementation-artifacts/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
  summary: Fixed transactions lack start/end dates in the domain model, preventing month-based filtering.
  evidence: Fixed transaction components had to bypass the month filtering logic completely because `dataInicio` does not exist on the current entity.
- source_spec: `_bmad-output/implementation-artifacts/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
  summary: Missing test coverage for React Query mutation cache invalidations and global month filter logic.
  evidence: No component or hook tests assert that `['consolidacao']` is invalidated or that `isGlobalFilterActive` works correctly.
- source_spec: `_bmad-output/implementation-artifacts/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
  summary: `epic-3-context.md` was translated to English, losing technical specificity and causing context fragmentation with Portuguese PRDs.
  evidence: The document omits explicit constraints like `ON DELETE RESTRICT` for foreign keys that were present in the Portuguese original.
