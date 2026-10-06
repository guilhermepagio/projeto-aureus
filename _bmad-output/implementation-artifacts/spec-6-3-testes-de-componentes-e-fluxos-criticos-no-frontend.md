---
title: 'Story 6.3: Testes de Componentes e Fluxos Críticos no Frontend'
type: 'feature'
created: '2026-10-05'
status: 'done'
baseline_commit: '6b855b5702e1caec8d779744549a39dc94f248ad'
review_loop_iteration: 0
context:
  - 'frontend-web/package.json'
  - 'frontend-web/vite.config.ts'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** A interface do Aureus V1 possui interações complexas de UI (recálculo dinâmico de parcelamentos, modais acessíveis, filtros globais temporais, confirmações de deleção e empty states) que necessitam de cobertura automatizada de testes de componentes para prevenir regressões de usabilidade e garantir conformidade com os critérios do Épico 6.

**Approach:** Desenvolver suítes de teste de componentes e fluxos com Vitest e React Testing Library cobrindo a reatividade dos formulários, componentes de suporte de UI (`EmptyState`, `ActionMenu`), modais de confirmação de exclusão (`DeleteConfirmModal`) e fluxos de listagem/filtro temporal das páginas (`DespesasVariaveisPage`, `ReceitasVariaveisPage`, `ContasPage`).

## Boundaries & Constraints

**Always:** Usar Vitest e React Testing Library (`@testing-library/react`, `@testing-library/user-event`). Manter mocks isolados por suíte de teste com limpeza via `afterEach(cleanup)`. Garantir que todos os testes executem em milissegundos via `npm test` no diretório `frontend-web`.

**Ask First:** Alterações em componentes de produção ou adição de dependências npm não previstas no `package.json`.

**Never:** Não introduzir dependências de E2E (Playwright/Cypress) nesta etapa (adiadas para pós-V1). Não acoplar testes de componentes a servidores ou APIs de backend reais.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Recálculo reativo de parcelamento | Digitação de valor `R$ 150,00` e `4` parcelas com início `2026-05` | Valor Total exibe `R$ 600,00` e Última Parcela exibe `Ago 2026 (08/2026)` instantaneamente | Inputs vazios ou inválidos exibem `-` sem lançar erro |
| Acessibilidade e Trapping do Modal | Modal aberto com foco no primeiro campo; tecla ESC ou clique no backdrop | Fecha modal via `onClose`; tecla Tab cicla foco internamente (Focus Trap) | Bloqueia fechamento se `disableClose=true` |
| Alternância de filtro temporal | Alterna switch `Ver Todos` (`isGlobalFilterActive`) | Lista exibe todas as movimentações sem filtro de mês; ao desativar, filtra por `selectedMonth` | Lista vazia renderiza `EmptyState` contextual |
| Confirmação de exclusão | Clique em Excluir no `ActionMenu` de uma linha | Abre `DeleteConfirmModal` com nome do item; Cancelar não muta; Confirmar dispara `mutate(id)` | Erro na mutação exibe mensagem de alerta no modal |
| EmptyState e Loading | `isLoading=true` ou lista vazia | Exibe texto de carregamento ou `EmptyState` com título, descrição e botão de ação contextual | Renderização defensiva com ícone padrão se nenhum fornecido |

</frozen-after-approval>

## Code Map

- `frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx` -- Testes dos formulários de movimentação fixa e variável e cálculos de parcelamento.
- `frontend-web/src/components/ui/Modal.test.tsx` -- Testes de acessibilidade, focus trap, ESC e fechamento do componente `Modal`.
- `frontend-web/src/components/ui/MonthPicker.test.tsx` -- Testes do seletor mensal popover com navegação e seleção.
- `frontend-web/src/components/ui/DatePicker.test.tsx` -- Testes do seletor de datas diárias com navegação e atalho Hoje.
- `frontend-web/src/components/ui/EmptyState.tsx` -- Componente de estado vazio da interface; alvo de novos testes unitários de renderização e ações.
- `frontend-web/src/components/ui/ActionMenu.tsx` -- Menu suspenso de ações em portal; alvo de novos testes de interação.
- `frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx` -- Modal de confirmação de exclusão; alvo de testes de fluxo de deleção e estados assíncronos.
- `frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx` -- Página de despesas variáveis com filtro global de mês e tabela; alvo de testes de fluxo e reatividade do filtro.
- `frontend-web/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx` -- Página de receitas variáveis com filtro temporal e alternância Ver Todos.
- `frontend-web/src/pages/Contas/ContasPage.tsx` -- Página de contas com tabela, estados de carregamento e empty state.
- `frontend-web/src/store/monthStore.ts` -- Store Zustand do mês selecionado e filtro global `isGlobalFilterActive`.

## Tasks & Acceptance

**Execution:**
- [x] `frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx` -- Adicionar teste de recálculo em tempo real de Valor Total e Última Parcela durante digitação interativa no modal variável -- Assegura AC 1 com interação de usuário.
- [x] `frontend-web/src/components/ui/EmptyState.test.tsx` -- Criar suíte de testes unitários do componente `EmptyState` -- Valida título, descrição, variantes de cores de botão e clique de ação (AC 6).
- [x] `frontend-web/src/components/ui/ActionMenu.test.tsx` -- Criar suíte de testes unitários do componente `ActionMenu` -- Valida acionamento do portal, opções Editar/Excluir e fechamento ao clicar fora (AC 5).
- [x] `frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.test.tsx` -- Criar suíte de testes do modal de confirmação de exclusão -- Valida exibição do item, cancelamento sem mutação, disparo de mutação ao confirmar, estado de loading (`Excluindo...`) e tratamento de erro (AC 5).
- [x] `frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.test.tsx` -- Criar suíte de testes de componente para a página de Despesas Variáveis -- Valida loading state, empty state contextual, abertura do modal de exclusão e alternância do filtro global temporal (AC 4, AC 5, AC 6).
- [x] `frontend-web/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.test.tsx` -- Criar suíte de testes de componente para a página de Receitas Variáveis -- Valida filtragem por mês de vigência e exibição total ao alternar switch Ver Todos (AC 4, AC 6).
- [x] `frontend-web/src/pages/Contas/ContasPage.test.tsx` -- Criar suíte de testes de componente para a página de Contas -- Valida loading, empty state com botão Nova Conta e renderização da tabela de contas (AC 6).

**Acceptance Criteria:**
- Given o formulário de movimentação variável aberto, when o usuário digita valor da parcela, quantidade de parcelas e seleciona o mês de início, then o Valor Total e a Última Parcela são recalculados e exibidos em tempo real na interface.
- Given qualquer diálogo/modal do sistema aberto, when acionado via ESC, clique no backdrop ou botão de fechar, then o diálogo é fechado e o foco é mantido contido durante a navegação por teclado.
- Given os componentes seletores personalizados `MonthPicker` e `DatePicker`, when acionados pelo usuário, then abrem seus respectivos popovers com navegação temporal e seleção precisa.
- Given a página de movimentações variáveis com filtro por mês ativo, when o usuário alternar o switch "Ver Todos", then todas as movimentações de todos os períodos são exibidas imediatamente na tabela sem recarregar a página.
- Given um registro na tabela de movimentações ou contas, when o usuário aciona o botão de exclusão no menu de ações, then um modal de confirmação explícita é apresentado e a mutação de exclusão só é disparada mediante confirmação do usuário.
### Review Findings

- [x] [Review][Patch] Refinar asserções de recálculo dinâmico, cobrir transição de ano (Nov/26 + 4 parcelas -> Fev/27) e valores 0/negativos [frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx:550]
- [x] [Review][Patch] Adicionar teste para variante padrão (primary) com prop color omitida [frontend-web/src/components/ui/EmptyState.test.tsx:99]
- [x] [Review][Patch] Validar bloqueio de fechamento via Escape quando isPending=true em DeleteConfirmModal [frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.test.tsx:135]
- [x] [Review][Patch] Adicionar restauração do store useMonthStore no afterEach e testes de alteração de mês e valores formatados [frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.test.tsx:216]
- [x] [Review][Patch] Adicionar restauração do store useMonthStore no afterEach e testes de alteração de mês e formatação [frontend-web/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.test.tsx:210]
- [x] [Review][Patch] Adicionar teste de confirmação e disparo de mutação de exclusão de conta [frontend-web/src/pages/Contas/ContasPage.test.tsx:147]
- [x] [Review][Defer] Suporte a tecla Escape e semântica WAI-ARIA no ActionMenu [frontend-web/src/components/ui/ActionMenu.tsx:45] — deferred, pre-existing
- [x] [Review][Defer] Unificação do DeleteConfirmModal compartilhado entre páginas [frontend-web/src/pages/] — deferred, pre-existing
- [x] [Review][Defer] Ajuste de dependência useEffect (deleteMutation) em DeleteConfirmModal [frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:18] — deferred, pre-existing

## Spec Change Log
- 2026-10-05: Implementação concluída com 18 arquivos de teste e 168 testes aprovados no frontend via Vitest e Testing Library.

## Design Notes

A estratégia de testes de frontend adota mocks de hooks de dados (`useDespesasVariaveis`, `useReceitasVariaveis`, `useContas`, etc.) e controle direto do store Zustand `useMonthStore`. Isso isola os componentes de chamadas de rede externas e permite testar as regras de transição de UI e acessibilidade em ambiente jsdom com máxima rapidez de execução.

## Verification

**Commands:**
- `cd frontend-web && npm test` -- expected: Todas as suítes de testes passam com 0 falhas.
- `cd frontend-web && npm run lint` -- expected: Zero erros de linting.
- `cd frontend-web && npm run build` -- expected: Compilação TypeScript e Vite bem-sucedida.

## Suggested Review Order

**Reatividade e Parcelamento em Formulários**

- Recálculo dinâmico de Valor Total e Última Parcela com transição de ano
  [`MovimentacaoForm.test.tsx:528`](../../frontend-web/src/components/MovimentacaoForm/MovimentacaoForm.test.tsx#L528)

**Componentes Visuais e de Ação**

- Cobertura unitária de renderização, classes de cor e disparos de ação
  [`EmptyState.test.tsx:1`](../../frontend-web/src/components/ui/EmptyState.test.tsx#L1)

- Interação do portal, opções Editar/Excluir e fechamento no clique fora
  [`ActionMenu.test.tsx:1`](../../frontend-web/src/components/ui/ActionMenu.test.tsx#L1)

- Confirmação de exclusão, bloqueio de escape em loading e tratamento de erro
  [`DeleteConfirmModal.test.tsx:1`](../../frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.test.tsx#L1)

**Fluxos Críticos e Filtros Temporais de Páginas**

- Filtro temporal reativo, switch Ver Todos e integração de modais em Despesas
  [`DespesasVariaveisPage.test.tsx:1`](../../frontend-web/src/pages/DespesasVariaveis/DespesasVariaveisPage.test.tsx#L1)

- Filtragem por período de vigência e alternância Ver Todos em Receitas
  [`ReceitasVariaveisPage.test.tsx:1`](../../frontend-web/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.test.tsx#L1)

- Renderização tabular, empty state com criação e mutação de exclusão de Contas
  [`ContasPage.test.tsx:1`](../../frontend-web/src/pages/Contas/ContasPage.test.tsx#L1)

