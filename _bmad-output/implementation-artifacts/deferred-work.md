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
