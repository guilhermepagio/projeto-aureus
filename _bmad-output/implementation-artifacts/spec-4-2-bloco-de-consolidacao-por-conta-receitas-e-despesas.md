---
title: 'Story 4.2: Bloco de Consolidação por Conta (Receitas e Despesas)'
type: 'feature'
created: '2026-08-25'
status: 'done'
baseline_commit: '5779219f89d7b0dd4734ceb634aa01468cc002c2'
review_loop_iteration: 1
context: ['_bmad-output/implementation-artifacts/epic-4-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** A matriz de 24 meses atualmente exibe dados "mockados" sem utilidade real, não refletindo o comportamento financeiro do usuário por conta.

**Approach:** Criar uma nova rota no backend para calcular a consolidação por conta num horizonte de 24 meses (projetando receitas/despesas fixas e variáveis). No frontend, consumir essa API via um hook customizado e substituir as linhas de simulação na matriz pelos totais reais de "Receitas por Conta" e "Despesas por Conta".

## Boundaries & Constraints

**Always:**
- A projeção de 24 meses deve ser calculada a partir do mês fornecido (`mesAno`) no backend, para evitar transferir grandes payloads e complexidade para o frontend.
- Despesas e Receitas Fixas se repetem do seu mês de início até o fim da projeção. Despesas e Receitas Variáveis ocorrem por `numeroParcelas` meses a partir da data de início.
- Células sem valor devem ser enviadas ou exibidas como `0.0` / `R$ 0,00`.
- O isolamento de dados do usuário logado deve ser respeitado no Controller (ex: filtrando por `Jwt jwt` ou extraindo `usuario_id`).

**Ask First:**
- Se for criar uma arquitetura complexa de `Service` em vez de adicionar o cálculo a uma nova estrutura coesa no pacote `service`.

**Never:**
- Não carregar todos os dados do banco para fazer os cálculos no frontend; a agregação deve ocorrer no backend.

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java` -- Novo controller para expor o endpoint `GET /api/consolidacao/por-conta`.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java` -- Novo DTO que encapsula as listas de consolidação de receitas e despesas por conta (array de 24 valores por conta).
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Novo serviço para orquestrar a busca das Contas e projetar as Despesas e Receitas nos próximos 24 meses a partir do `mesAnoInicio`.
- `frontend/src/hooks/useConsolidacao.ts` -- Novo hook usando React Query para buscar a consolidação baseada no `selectedMonth`.
- `frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx` -- Alteração para remover as linhas de "Categoria X" mockadas e mapear sobre os dados reais da API, agrupando em "Receitas" e "Despesas".

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java` -- Criar DTOs para retornar as linhas (`contaId`, `contaDescricao`, `List<BigDecimal> valoresMensais`).
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Implementar o algoritmo de consolidação recebendo `usuarioId`. **OBRIGATÓRIO:** Buscar contas e movimentações filtrando sempre por `usuarioId` (`findByUsuarioId`). Projetar Despesas e Receitas Fixas repetindo o valor a partir da data de início (`dataInicio`) até o fim dos 24 meses. Prevenir NullPointerException caso falte dataInicio.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java` -- Expor endpoint `GET /api/consolidacao/por-conta`. **OBRIGATÓRIO:** Validar o formato `mesAno` (ex: via regex `@Pattern`) e extrair o ID do usuário (ex: `@AuthenticationPrincipal Jwt jwt`) para repassar ao Service.
- [x] `backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java` -- (NOVO) Criar testes de integração/unidade garantindo que o endpoint filtra por usuário (isolamento) e rejeita `mesAno` inválido.
- [x] `frontend/src/hooks/useConsolidacao.ts` -- Criar hook usando React Query. **OBRIGATÓRIO:** Utilizar a configuração de API/fetch existente no projeto com o token adequado, evitando adicionar pacotes redundantes como `axios` isoladamente se já houver um padrão. Lidar com token nulo graciosamente.
- [x] `frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx` -- Usar o hook. Renderizar dinamicamente os blocos. Otimizar `formatCurrency` definindo o formatter fora do loop para performance. Tratar `colSpan` dinamicamente ou corretamente para não quebrar layout, e não usar plain `<div>` para loading/erro que quebre a tag `<table>`.

**Acceptance Criteria:**
- Given a grade de 24 meses carregada com o mês inicial escolhido, when a requisição termina, then o Bloco Receitas exibe a soma de receitas fixas e variáveis para cada conta.
- Given o endpoint de consolidação, when chamado, then apenas os dados pertencentes ao usuário logado são processados.
- Given o processamento de itens fixos, when a `dataInicio` do item for posterior ao mês corrente da projeção, then o valor não é somado naquele mês.
- Given a ausência de token, when o hook executa, then não envia requisições com "Bearer null".
- Given a estrutura do projeto, when a branch for finalizada, then o arquivo `seed.sql` NÃO FOI apagado ou modificado indevidamente.

### Review Findings
- [x] [Review][Patch] Testes ausentes para a validação do @Pattern e autenticação do usuarioId [ConsolidacaoControllerTest.java]
- [x] [Review][Patch] Bug no fuso horário instanciando a data local [frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx:22]
- [x] [Review][Patch] Exceção ConstraintViolationException não tratada para falhas do @Pattern [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java]
- [x] [Review][Patch] Ausência de testes de unidade para o algoritmo de projeção de 24 meses [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java]

## Spec Change Log

- **Trigger:** Edge Case & Verification Gap review - Falta de isolamento de usuário (`findAll()`), deleção acidental de `seed.sql`, ignoração de `dataInicio` para movimentações fixas, falta de testes.
- **Amended:** Adicionadas tarefas explícitas em `## Tasks & Acceptance` para forçar validação de `@Pattern`, filtragem rigorosa por `usuarioId`, tratamento de `dataInicio` em itens fixos, proteção de layouts de erro em tabelas, otimização de React e obrigatoriedade de criação de testes unitários/integração backend.
- **Avoided state:** Endpoint devolvendo dados de múltiplos tenants de forma indiscriminada (vazamento de dados massivo), NPEs em projeções e erros 500 sem tratamento. Deleção de arquivos de desenvolvimento (`seed.sql`).
- **KEEP:** Manter a estrutura de DTO (`ConsolidacaoPorContaDTO`, `LinhaConsolidacaoDTO`) e a arquitetura geral de Service e Controller. Utilizar React Query e a separação nos componentes do Grid.

## Verification

**Commands:**
- `cd frontend && npm run build` -- expected: Compila sem erros no frontend.
- `cd backend && ./mvnw clean test` -- expected: Testes (se existirem) não quebram no backend e o projeto compila.

## Suggested Review Order

**Backend Core Logic**

- Algoritmo de projeção financeira de 24 meses.
  [`ConsolidacaoService.java:30`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java#L30)

- Definição do endpoint, validação de mês e injeção do usuário logado.
  [`ConsolidacaoController.java:21`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java#L21)

- Formato de dados consolidados enviado ao frontend.
  [`ConsolidacaoPorContaDTO.java:10`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java#L10)

**Frontend Integration**

- Hook do React Query para buscar a consolidação com CSRF.
  [`useConsolidacao.ts:25`](../../frontend/src/hooks/useConsolidacao.ts#L25)

- Renderização dinâmica do Grid de consolidação de 24 meses.
  [`ConsolidacaoGrid.tsx:30`](../../frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx#L30)

**Persistence & Tests**

- Adição de dataInicio para suportar projeção temporal nas entidades fixas.
  [`DespesaFixa.java:22`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java#L22)

- Isolamento explícito de tenant nos repositórios.
  [`ContaRepository.java:12`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java#L12)

- Testes de isolamento do controller.
  [`ConsolidacaoControllerTest.java:28`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java#L28)
