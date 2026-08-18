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
