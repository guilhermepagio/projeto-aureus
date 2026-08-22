---
title: 'Epic 3 Story 4: Sincronização de Visão Mensal, Invalidação de Cache e Filtro Global'
type: 'feature'
created: '2026-08-22'
status: 'done'
baseline_commit: '3aebf95bb5c6e5bb3a666b68e28850d58a9787ef'
review_loop_iteration: 0
context: 
  - '{project-root}/_bmad-output/implementation-artifacts/epic-3-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** As listas de lançamentos financeiros não filtram os dados por mês ativo e as mutações não invalidam o cache das outras listagens e da visão consolidada, causando dessincronização de dados.

**Approach:** Criar um store Zustand para gerenciar o mês ativo global e um toggle de filtro global. Implementar um componente reutilizável de `EmptyState`. Atualizar as listagens de despesas e receitas para filtrar pelo mês ativo e reagir ao toggle de filtro. Refatorar os hooks de mutação do React Query para invalidarem todas as queries relevantes (incluindo consolidação) após o sucesso.

## Boundaries & Constraints

**Always:**
- Utilizar Zustand para o estado global `monthStore`.
- Invalidar a query de consolidação (`['consolidacao']`) e todas as listas de lançamentos afetadas após qualquer mutação (create, edit, delete).
- Renderizar o novo componente reutilizável `EmptyState` em listas sem registros.

**Ask First:**
- Se for necessário mudar o layout do componente Header global para acomodar os controles de mês/filtro.
- Se o formato de data (ex: `YYYY-MM`) no Zustand precisar ser diferente de string ou Date para os componentes atuais.

**Never:**
- Não gerenciar o estado global de mês usando `useState` ou Context API (somente Zustand).
- Não realizar recarregamento de página (`window.location.reload`) após mutações.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Mês com dados selecionado | Zustand `selectedMonth` = "2026-08", Toggle Global Filter = off | Listas mostram apenas lançamentos vigentes em Agosto/2026. | N/A |
| Mês vazio selecionado | Zustand `selectedMonth` = "2026-09" (sem dados), Toggle Global Filter = off | Listas exibem o componente `EmptyState`. | N/A |
| Filtro Global Ativado | Toggle Global Filter = on, `selectedMonth` = "2026-09" | Listas ignoram `selectedMonth` e mostram todos os lançamentos de todos os meses. | N/A |
| Mutação de Lançamento | Sucesso na deleção/criação/edição | `queryClient.invalidateQueries` é chamado para a chave do lançamento e para `['consolidacao']`. A lista atualiza automaticamente. | O cache não é invalidado se a mutação falhar. |

</frozen-after-approval>

## Code Map

- `frontend/src/store/monthStore.ts` -- (A ser criado) Store Zustand contendo o mês ativo (`selectedMonth`) e o estado do toggle `isGlobalFilterActive`.
- `frontend/src/components/ui/EmptyState.tsx` -- (A ser criado) Componente reutilizável para exibição de estado vazio nas listas.
- `frontend/src/hooks/useDespesasVariaveis.ts` -- Refatorar `useCreate...`, `useUpdate...`, `useDelete...` para chamarem `queryClient.invalidateQueries({ queryKey: ['consolidacao'] })` (além das suas próprias keys).
- `frontend/src/hooks/useReceitasVariaveis.ts` -- Idem acima.
- `frontend/src/hooks/useDespesasFixas.ts` -- Idem acima.
- `frontend/src/hooks/useReceitasFixas.ts` -- Idem acima.
- `frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx` -- Aplicar filtro por mês (ou bypass se global filter true) nos dados antes de renderizar; implementar botão toggle de filtro global e uso do `EmptyState`.
- `frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx` -- Idem acima.
- `frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx` -- Idem acima.
- `frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx` -- Idem acima.
- `frontend/src/pages/Contas/ContasPage.tsx` -- Refatorar para usar o novo `EmptyState` em vez da marcação duplicada.
- `frontend/src/pages/Categorias/CategoriasPage.tsx` -- Refatorar para usar o novo `EmptyState` em vez da marcação duplicada.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/store/monthStore.ts` -- Criar store Zustand com estado para `selectedMonth` e `isGlobalFilterActive` e ações para atualizá-los. -- Necessário para gerenciamento do filtro de meses entre as abas.
- [x] `frontend/src/components/ui/EmptyState.tsx` -- Criar componente `EmptyState` recebendo props como `title`, `description`, `icon` e `action`. -- Padronização visual para listas vazias exigida pela Epic 3 e limpeza do código duplicado nas páginas.
- [x] `frontend/src/hooks/useDespesasVariaveis.ts` -- Atualizar funções `onSuccess` das mutações para invalidarem também a query `['consolidacao']`. -- Garantir que o painel de consolidação da Epic 4 atualize quando despesas mudarem.
- [x] `frontend/src/hooks/useReceitasVariaveis.ts` -- Atualizar funções `onSuccess` das mutações para invalidarem também a query `['consolidacao']`.
- [x] `frontend/src/hooks/useDespesasFixas.ts` -- Atualizar funções `onSuccess` das mutações para invalidarem também a query `['consolidacao']`.
- [x] `frontend/src/hooks/useReceitasFixas.ts` -- Atualizar funções `onSuccess` das mutações para invalidarem também a query `['consolidacao']`.
- [x] `frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx` -- Consumir `monthStore`, filtrar a array de dados `data` com base no `selectedMonth` (a menos que `isGlobalFilterActive` seja true), renderizar Toggle do filtro, e usar `EmptyState`. -- Cumprir critérios de visualização da Story 3.4.
- [x] `frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx` -- Idem acima.
- [x] `frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx` -- Idem acima.
- [x] `frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx` -- Idem acima.
- [x] `frontend/src/pages/Contas/ContasPage.tsx` -- Substituir marcação inline pelo `<EmptyState />`. -- Remoção de débito técnico/código duplicado.
- [x] `frontend/src/pages/Categorias/CategoriasPage.tsx` -- Substituir marcação inline pelo `<EmptyState />`. -- Remoção de débito técnico/código duplicado.

**Acceptance Criteria:**
- Given a página de listagem de Lançamentos (ex: Despesas Variáveis), when o mês X for selecionado no store e o toggle global estiver desativado, then a lista mostrará somente os lançamentos com competência/vigência no mês X e um EmptyState caso não haja.
- Given a página de listagem de Lançamentos, when o toggle global (Ver Todos) estiver ativado, then o filtro de mês será desconsiderado e todos os lançamentos cadastrados serão exibidos.
- Given o cadastro, edição ou deleção de um Lançamento, when a mutação obtém sucesso, then os caches da própria entidade afetada e do painel de Consolidação são invalidados, atualizando todas as views.

## Spec Change Log

## Verification

**Commands:**
- `npm run lint` na pasta `frontend` -- expected: Sem erros de tipagem ou de regras do React.
- `npm run build` na pasta `frontend` -- expected: Build completa sem erros do TypeScript.

## Suggested Review Order

**Gerenciamento de Estado Global**

- Criação da store global de mês com Zustand
  [`monthStore.ts:1`](../../frontend/src/store/monthStore.ts#L1)

**Filtro em Tempo de Execução e UI (Despesas/Receitas Variáveis)**

- Lógica de filtro temporal baseada no mês ativo (ou bypass se toggle global)
  [`DespesasVariaveisPage.tsx:35`](../../frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx#L35)

- Toggle UI do Filtro Global (Ver Todos) acessível
  [`DespesasVariaveisPage.tsx:60`](../../frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx#L60)

**Filtro e Tratamento (Fixas)**

- Omissão técnica do filtro de mês (bypassed pois não suportado pela entidade)
  [`DespesasFixasPage.tsx:30`](../../frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx#L30)

**Componente Reutilizável de Estado Vazio**

- Adição de prop de cor e estrutura padronizada de UI vazia
  [`EmptyState.tsx:20`](../../frontend/src/components/ui/EmptyState.tsx#L20)

- Substituição de código duplicado nas listas fixas e contas
  [`ContasPage.tsx:40`](../../frontend/src/pages/Contas/ContasPage.tsx#L40)

**Invalidação de Cache (React Query)**

- Invalidação síncrona da query de consolidação nas mutações
  [`useDespesasVariaveis.ts:114`](../../frontend/src/hooks/useDespesasVariaveis.ts#L114)
