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
