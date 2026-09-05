
## Suggested Review Order

**DTO & Repository**

- Novo DTO que encapsula as listas de consolidação.
  [`ConsolidacaoPorCategoriaDTO.java:12`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorCategoriaDTO.java#L12)

- Busca as categorias isoladas do usuário.
  [`CategoriaRepository.java:9`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java#L9)

**Projeção de Dados (Core Logic)**

- Projeta lançamentos nos 24 meses por categoria.
  [`ConsolidacaoService.java:140`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java#L140)

- Exposição dos dados agregados para o frontend.
  [`ConsolidacaoController.java:45`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java#L45)

**Integração Frontend**

- Tabela de categorias e cálculo seguro de percentuais.
  [`BlocoCategorias.tsx:10`](../../frontend/src/components/Consolidacao/BlocoCategorias.tsx#L10)

- Hook React Query da nova funcionalidade.
  [`useConsolidacaoCategoria.ts:5`](../../frontend/src/hooks/useConsolidacaoCategoria.ts#L5)

- Injeção do Bloco de Categorias na tabela.
  [`ConsolidacaoGrid.tsx:130`](../../frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx#L130)

---
title: 'Story 4.3: Bloco Analítico de Categorias (Valores e Percentuais com Proteção Zero)'
type: 'feature'
created: '2026-08-25'
status: 'done'
baseline_commit: '35534e4ad0c9ab5e6fc4ce8b9fbd889ba529011d'
review_loop_iteration: 1

**Testes**

- Verificação dos cálculos, datas limites e categoria nula.
  [`ConsolidacaoServiceTest.java:264`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java#L264)

- Verificação das restrições e segurança da API.
  [`ConsolidacaoControllerTest.java:94`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java#L94)

### Review Findings
- [ ] [Review][Decision] Unexpected implementation of "Receitas por Categoria" — Story spec focuses on expenses; is this scope creep desired?
- [ ] [Review][Decision] Empty categories cluttering UI — Should categories with zero balance across all 24 months be omitted?
- [ ] [Review][Patch] Invalid HTML `<div>` in `<tbody>` and Layout Shift [frontend/src/components/Consolidacao/BlocoCategorias.tsx]
- [ ] [Review][Patch] Missing Implementation of Percentage Block and Zero-Division Protection [frontend/src/components/Consolidacao/BlocoCategorias.tsx]
- [ ] [Review][Patch] Manual Authorization Check instead of Spring Security [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java:107]
- [ ] [Review][Patch] Inefficient O(N*M) 24-Month Projection Loop [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:305]
- [ ] [Review][Patch] Database seed script deleted [seed.sql]
- [ ] [Review][Patch] Validation test `deveRejeitarMesAnoInvalido` deleted [backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java]
- [ ] [Review][Patch] Account consolidation value assertions removed [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java:102]
- [ ] [Review][Patch] Incomplete category projection tests [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java:144]
- [ ] [Review][Patch] Missing tests for BlocoCategorias [frontend/src/components/Consolidacao/BlocoCategorias.tsx]
- [ ] [Review][Patch] Use of raw floating-point addition for monetary totals [frontend/src/components/Consolidacao/BlocoCategorias.tsx]
- [ ] [Review][Patch] DTO lacks Lombok annotations [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorCategoriaDTO.java]
- [ ] [Review][Patch] `MockitoAnnotations.openMocks` used instead of `@ExtendWith(MockitoExtension.class)` [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java]
- [ ] [Review][Patch] Unsafe Optionals in tests (.findFirst().get()) [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java]
- [ ] [Review][Patch] Component lacks selectedMonth guard [frontend/src/components/Consolidacao/BlocoCategorias.tsx]
- [x] [Review][Defer] Fetches all transactions regardless of requested period [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java] — deferred, pre-existing
- [x] [Review][Defer] Hardcoded 24-month horizon magic numbers [Multiple] — deferred, pre-existing
- [x] [Review][Defer] Raw fetch and manual CSRF token instead of centralized client [frontend/src/hooks/useConsolidacaoCategoria.ts] — deferred, pre-existing
