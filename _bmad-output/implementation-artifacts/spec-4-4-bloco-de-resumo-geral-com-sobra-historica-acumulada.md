---
title: 'Story 4.4: Bloco de Resumo Geral com Sobra Histórica Acumulada'
type: 'feature'
created: '2026-09-05'
status: 'done'
baseline_commit: '34e415d03bb0fe9920cec15409d396a47f605ef7'
review_loop_iteration: 4
context: ['_bmad-output/implementation-artifacts/epic-4-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O painel de 24 meses consolida contas e categorias, mas não exibe os totais consolidados mensais, o resultado líquido ("Sobra do Mês") e a evolução patrimonial acumulada considerando o histórico financeiro prévio ("Sobra Retroativa Acumulada"). Além disso, o posicionamento atual do seletor de mês e do botão "Mês Atual" está desalinhado em relação ao design de referência do mockup desktop.

**Approach:** Estender a API de consolidação por conta para calcular e retornar o saldo histórico líquido acumulado anterior ao primeiro mês da grade (`saldoHistoricoPreGrade`). No frontend, renderizar o "Bloco Resumo Geral" computando "Total Gasto no Mês", "Sobra do Mês" e "Sobra Retroativa Acumulada" ao longo dos 24 meses. Adicionalmente, reestruturar a toolbar da Consolidação para posicionar o título "Consolidação", o seletor de mês e o botão "Mês Atual" lado a lado à esquerda, idêntico ao mockup desktop (`consolidacao-desktop.html`).

## Boundaries & Constraints

**Always:**
- O cálculo do saldo histórico pré-grade deve ser feito no backend, considerando apenas registros pertencentes ao usuário autenticado (`findByUsuarioId`).
- Parcelas de lançamentos variáveis anteriores ao `startMonth` devem ser contabilizadas no saldo histórico proporcionalmente às parcelas vencidas antes de `startMonth`.
- Lançamentos fixos com `dataInicio` anterior ao `startMonth` têm seus meses decorridos até `startMonth` contabilizados no saldo histórico. Registros sem `dataInicio` não geram histórico pré-grade retroativo infinito.
- No frontend, "Total Gasto" é a soma de despesas do mês; "Sobra do Mês" é Receitas do Mês - Despesas do Mês; "Sobra Retroativa Acumulada" soma cumulativamente a sobra de cada mês iniciando com o saldo histórico pré-grade.
- Valores monetários devem ser formatados via `formatCurrency` e com classes visuais semânticas (verde/teal para positivo, vermelho para negativo, neutro para zero).
- O seletor de mês e o botão "Mês Atual" devem estar alinhados na mesma linha do título "Consolidação" à esquerda (`toolbar-left`), adotando o botão dourado/amber `#D4A843` e seletor estilizado conforme o mockup desktop.

**Ask First:**
- Se houver necessidade de criar novas tabelas ou migrações de banco de dados (o cálculo deve ser derivado dos lançamentos existentes).

**Never:**
- Não calcular o saldo histórico carregando dados de outros usuários nem quebrar o isolamento de tenant.
- Não utilizar comentários no código referenciando epics ou stories (ex: `# Epic 4`).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Sem histórico pré-grade | Usuário inicia lançamentos em 2024-05, grid em 2024-05 | `saldoHistoricoPreGrade = 0.00`. Mês 1 acumulado = `Sobra Mês 1` | Retorna 0.00 graciosamente |
| Com histórico de parcelas e fixos | Despesa fixa 50/mês desde 2024-01, receita variável 100/mês (3 parcelas Jan-Mar), grid em 2024-03 | Saldo histórico = (2 parcelas de receita: 200) - (2 meses de despesa fixa: 100) = +100.00 | Cálculos com `BigDecimal` e arredondamento seguro |
| Grid com meses zerados | Mês sem receitas nem despesas | Total Gasto = 0,00, Sobra = 0,00, Sobra Retroativa repete o saldo acumulado anterior | Exibição neutra, sem divisão ou erro |
| Sobra do Mês negativa | Despesas > Receitas no mês | Valor exibido em vermelho (`text-red-600`), reduzindo a sobra acumulada | Trata sinal negativo no formatador e classes |
| Posicionamento Toolbar | Acesso à tela de Consolidação | Título, seletor de mês e botão "Mês Atual" alinhados lado a lado à esquerda | Layout responsivo flexível |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java` -- Adição do campo `BigDecimal saldoHistoricoPreGrade` com construtor de compatibilidade para preservar chamadas existentes.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Implementação do cálculo de `saldoHistoricoPreGrade` agregando receitas e despesas históricas (fixas e variáveis) anteriores a `startMonth`.
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java` -- Testes unitários cobrindo o cálculo do saldo histórico pré-grade em múltiplos cenários.
- `backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java` -- Testes de API garantindo serialização de `saldoHistoricoPreGrade`.
- `frontend/src/hooks/useConsolidacao.ts` -- Tipagem atualizada de `ConsolidacaoPorContaDTO` com `saldoHistoricoPreGrade?: number`.
- `frontend/src/components/Consolidacao/ConsolidacaoToolbar.tsx` -- Reestruturação da toolbar para agrupar título "Consolidação", seletor de mês e botão "Mês Atual" (estilo `#D4A843`) lado a lado à esquerda idêntico a `consolidacao-desktop.html`.
- `frontend/src/pages/Consolidacao/ConsolidacaoPage.tsx` -- Limpeza do cabeçalho da página para harmonizar com a nova toolbar unificada.
- `frontend/src/components/Consolidacao/BlocoResumo.tsx` -- Novo componente para renderizar o cabeçalho "Resumo Geral" e as três linhas de consolidação (Total Gasto, Sobra do Mês, Sobra Retroativa Acumulada).
- `frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx` -- Injeção do `<BlocoResumo />` no corpo da tabela após o bloco de categorias.

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java` -- Atualizar DTO para conter `BigDecimal saldoHistoricoPreGrade` mantendo sobrecarga de construtor para chamadas com 2 parâmetros.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Calcular no método `calcularConsolidacaoPorConta` o somatório histórico de receitas e despesas anteriores a `YearMonth.parse(mesAno)` para o usuário autenticado.
- [x] `backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java` -- Adicionar casos de teste para saldo histórico com despesas fixas, variáveis, receitas e ausência de histórico.
- [x] `backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java` -- Testes de API garantindo serialização de `saldoHistoricoPreGrade`.
- [x] `frontend/src/hooks/useConsolidacao.ts` -- Atualizar tipagem TypeScript para expor `saldoHistoricoPreGrade?: number`.
- [x] `frontend/src/components/Consolidacao/ConsolidacaoToolbar.tsx` -- Agrupar título "Consolidação", seletor de mês e botão "Mês Atual" (cor amber `#D4A843`) lado a lado à esquerda conforme `consolidacao-desktop.html`.
- [x] `frontend/src/pages/Consolidacao/ConsolidacaoPage.tsx` -- Atualizar integração da toolbar na página de Consolidação.
- [x] `frontend/src/components/Consolidacao/BlocoResumo.tsx` -- Criar componente modular com a tabela de resumo (Total Gasto, Sobra do Mês, Sobra Retroativa Acum.) com estilizações conforme mockup.
- [x] `frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx` -- Integrar `BlocoResumo` com separador visual abaixo de `BlocoCategorias`.

### Review Findings
- [x] [Review][Patch] Garantir paridade de contas entre grid e saldo histórico em ConsolidacaoService [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:148]
- [x] [Review][Patch] Corrigir teste testSaldoHistoricoSemDataInicioNaoGeraRetroativo associando Conta aos fixtures [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java:243]
- [x] [Review][Patch] Adicionar teste para saldo histórico pré-grade negativo [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java:264]
- [x] [Review][Patch] Tornar detecção de números negativos mais precisa e defensiva em currencyFormat.ts [frontend/src/utils/currencyFormat.ts:10]
- [x] [Review][Patch] Adicionar aria-hidden="true" aos labels visuais adjacentes a sr-only em BlocoResumo.tsx [frontend/src/components/Consolidacao/BlocoResumo.tsx:99]
- [x] [Review][Patch] Adicionar fallback defensivo de carregamento e erro em BlocoResumo.tsx quando usado standalone [frontend/src/components/Consolidacao/BlocoResumo.tsx:21]
- [x] [Review][Patch] Remover espaçamento mb-4 duplicado entre ConsolidacaoPage e ConsolidacaoToolbar [frontend/src/pages/Consolidacao/ConsolidacaoPage.tsx:6]
- [x] [Review][Patch] Criar testes unitários para currencyFormat.ts [frontend/src/utils/currencyFormat.test.ts:1]
- [x] [Review][Patch] Criar teste de integração para ConsolidacaoGrid verificando renderização do BlocoResumo [frontend/src/components/Consolidacao/ConsolidacaoGrid.test.tsx:1]
- [x] [Review][Patch] Adicionar teste unitário para parcelas variáveis com prazo anterior a startMonth (between > quantidadeParcelas) [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java:230]
- [x] [Review][Patch] Adicionar asserção de valor numérico de Sobra Retroativa Acumulada da primeira coluna em ConsolidacaoGrid.test.tsx [frontend/src/components/Consolidacao/ConsolidacaoGrid.test.tsx:60]
- [x] [Review][Patch] Adicionar tratamento defensivo para tipos não-string e caracteres de menos Unicode em currencyFormat.ts [frontend/src/utils/currencyFormat.ts:1]
- [x] [Review][Patch] Adicionar encadeamento opcional d?.valoresMensais no cálculo de receitas e despesas em BlocoResumo.tsx [frontend/src/components/Consolidacao/BlocoResumo.tsx:51]
- [x] [Review][Patch] Substituir referências qualificadas por imports estáticos em ConsolidacaoControllerTest.java [backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java:97]
- [x] [Review][Defer] Divergência na agregação entre contas e categorias para lançamentos com conta nula [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:140] — deferred, pre-existing
- [x] [Review][Patch] Inicializar saldoHistoricoPreGrade com default zero no campo de ConsolidacaoPorContaDTO [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java:17]
- [x] [Review][Patch] Incluir suporte a em-dash (—) no regex de negativos em currencyFormat.ts [frontend/src/utils/currencyFormat.ts:1]
- [x] [Review][Patch] Alinhar texto acessível sr-only com texto visual 'Total Gasto no Mês' em BlocoResumo.tsx [frontend/src/components/Consolidacao/BlocoResumo.tsx:109]
- [x] [Review][Patch] Adicionar vi.restoreAllMocks() no afterEach de BlocoResumo.test.tsx e ConsolidacaoGrid.test.tsx [frontend/src/components/Consolidacao/BlocoResumo.test.tsx:16]
- [x] [Review][Patch] Cache do formatador Intl.NumberFormat em nível de módulo em currencyFormat.ts [frontend/src/utils/currencyFormat.ts:25]
- [x] [Review][Patch] Adicionar teste para limitação de parcelas históricas de ReceitaVariavel em ConsolidacaoServiceTest.java [backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java:285]
- [x] [Review][Patch] Proteger construtor de ConsolidacaoPorContaDTO contra argumento saldoHistoricoPreGrade nulo [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java:17]
- [x] [Review][Patch] Evitar exibição prematura de erro em BlocoResumo.tsx quando selectedMonth for nulo ou consulta desabilitada [frontend/src/components/Consolidacao/BlocoResumo.tsx:25]
- [x] [Review][Patch] Simplificar container flex removendo justify-between redundante em ConsolidacaoToolbar.tsx [frontend/src/components/Consolidacao/ConsolidacaoToolbar.tsx:43]
- [x] [Review][Patch] Reorganizar hooks incondicionalmente no topo de BlocoResumo.tsx conforme as Rules of Hooks [frontend/src/components/Consolidacao/BlocoResumo.tsx:20]
- [x] [Review][Patch] Implementar setter explícito com fallback para zero em ConsolidacaoPorContaDTO.java [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ConsolidacaoPorContaDTO.java:17]
- [x] [Review][Patch] Adicionar testes unitários para os estados de loading e error em BlocoResumo.test.tsx [frontend/src/components/Consolidacao/BlocoResumo.test.tsx:90]

**Acceptance Criteria:**
- Given todos os blocos anteriores calculados, when as linhas de resumo da Consolidação são renderizadas, then a linha "Total Gasto" exibe a somatória de todas as despesas do mês.
- Given o cálculo mensal, when a linha "Sobra do Mês" é renderizada, then exibe a diferença `Receitas do Mês - Despesas do Mês`.
- Given o endpoint `GET /api/consolidacao/por-conta`, when executado para um `mesAno`, then o backend calcula e envia `saldoHistoricoPreGrade` refletindo a movimentação líquida anterior ao primeiro mês da grade.
- Given o primeiro mês da grade, when a linha "Sobra Retroativa Acumulada" é renderizada, then computa `Saldo Histórico Pré-Grade + Sobra do Mês 1`.
- Given os meses subsequentes `n` (de 2 a 24), when a linha "Sobra Retroativa Acumulada" é renderizada, then computa `Sobra Retroativa Mês n = Sobra Retroativa Mês n-1 + Sobra do Mês n`.
- Given a tela de Consolidação, when renderizada, then o título "Consolidação", o seletor de mês e o botão "Mês Atual" são exibidos lado a lado alinhados à esquerda na toolbar conforme o mockup `consolidacao-desktop.html`.

## Verification

**Commands:**
- `backend/./mvnw test` -- expected: BUILD SUCCESS com todos os testes passando
- `cd frontend && npm run build` -- expected: compilação TypeScript e bundle Vite com status 0

### Review Findings & Patches
- **Triage**: 3 review subagents executed (`blind-hunter`, `edge-case-hunter`, `verification-gap`). 0 intent gaps, 0 bad spec issues.
- **Backend Patch**: Added `item.getConta() != null` checks in historical calculation loops (`ConsolidacaoService.java`) ensuring consistent account filtering between grid columns and historical accumulation.
- **Frontend Patches**:
  - Normalized zero formatting in `currencyFormat.ts` to prevent `-R$ 0,00` and ensure robust negative string parsing.
  - Guarded `saldoHistoricoPreGrade` with `Number(...) || 0`, memoized monthly totals and cumulative surplus with `useMemo`, and converted hidden text to `sr-only` for accessibility in `BlocoResumo.tsx`.
- **Automated Verification**:
  - `cd backend && ./mvnw test` passed (15 tests passed, 0 failures).
  - `cd frontend && npm test -- --run` passed (4 test files, 10 tests passed, 0 failures).
  - `cd frontend && npm run build` passed (Vite production bundle built in 945ms).
