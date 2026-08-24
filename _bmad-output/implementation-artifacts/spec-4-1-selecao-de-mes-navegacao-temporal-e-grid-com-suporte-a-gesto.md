---
title: 'Story 4.1: Seleção de Mês, Navegação Temporal e Grid'
type: 'feature'
created: '2026-08-24'
status: 'done'
baseline_commit: 'e0381c653013a8d59d0352953c27979dfcede5fc'
review_loop_iteration: 0
context: ['_bmad-output/implementation-artifacts/epic-4-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O painel de Consolidação atualmente é um placeholder e o usuário não consegue visualizar seu horizonte financeiro. Precisamos de uma matriz interativa que inicie num mês específico e projete 24 meses futuros/passados com navegação fluida em Desktop e Mobile.

**Approach:** Criar a página de Consolidação (`ConsolidacaoPage`), contendo uma "Toolbar" (título, botões prev/next mês e botão "Mês Atual") e o componente base do grid (`ConsolidacaoGrid`). O grid utilizará uma estrutura de tabela (`<table>`) nativa dentro de um container com scroll horizontal. A tabela terá 24 colunas de meses de dados mais a primeira coluna fixa de rótulos. A interação mobile se apoiará em scroll horizontal nativo.

## Boundaries & Constraints

**Always:** A estrutura do grid deve replicar a semântica de tabela (usando `table`, `thead`, `tbody`, `tr`, `th`, `td`) conforme o wireframe referenciado (`consolidacao-desktop.html`). A primeira coluna (`th:first-child`, `td:first-child`) e o cabeçalho superior devem usar posicionamento `sticky` para se manterem visíveis. O container da tabela deve utilizar `overflow-x-auto` para rolagem horizontal nativa.

**Ask First:** Adicionar dependências pesadas de drag/drop ou gestos além do scroll nativo css.

**Never:** Não carregar nem processar dados reais nesta story. O foco é apenas no layout (Toolbar), integração com o store para navegação de mês, e renderização da tabela HTML base das 24 colunas com datas corretas no cabeçalho e mock provisório de blocos.

</frozen-after-approval>

## Code Map

- `frontend/src/App.tsx` -- Atualizar a rota `/` (Consolidação) para utilizar o novo `ConsolidacaoPage` em vez do placeholder embutido.
- `frontend/src/store/monthStore.ts` -- Já existe; gerencia `selectedMonth`. Será lido/escrito pelo MonthPicker e ditará o mês inicial do grid.
- `frontend/src/pages/Consolidacao/ConsolidacaoPage.tsx` -- Orquestra a página conectando o estado global ao seletor de mês e repassa ao grid.
- `frontend/src/components/ui/MonthPicker.tsx` -- Componente isolado para selecionar o mês (`input type="month"`) e atalho "Mês Atual", interagindo com onChange.
- `frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx` -- Componente responsável por renderizar a estrutura CSS (primeira coluna sticky, próximas 24 colunas roláveis). Cada cabeçalho deve exibir o mês/ano correspondente (ex: Jan/25, Fev/25).

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/components/ui/MonthPicker.tsx` -- Criar componente Toolbar contendo os botões (setas e "Mês Atual") e exibição do mês (`Ago 2026`). Pode ser renomeado para algo como `ConsolidacaoToolbar.tsx` ou englobado na página, já que é específico.
- [x] `frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx` -- Criar layout de tabela HTML (`<table>`) encapsulada em `div` com `overflow-x-auto`. O `<thead>` deve renderizar a célula em branco e os 24 meses (`th`). O `<tbody>` terá linhas falsas temporárias para validar o layout e o `sticky` do `td:first-child` e `th`. Usar classes Tailwind equivalentes ao wireframe ou as próprias variáveis CSS.
- [x] `frontend/src/pages/Consolidacao/ConsolidacaoPage.tsx` -- Criar a view que junta Header da tela, a Toolbar de seleção de mês e o `ConsolidacaoGrid`, conectando a alteração de mês com o `monthStore`.
- [x] `frontend/src/App.tsx` -- Substituir placeholder pelo `ConsolidacaoPage`.

**Acceptance Criteria:**
- Given que o usuário acessa a aba "Consolidação", when a tela carrega, then a matriz renderiza horizontalmente 24 colunas de meses a partir do mês definido no store.
- Given o grid carregado, when o usuário rola horizontalmente, then a primeira coluna lateral (Contas/Categorias) permanece fixa (`sticky`).
- Given a navegação mobile, when o usuário arrasta o dedo no grid horizontalmente (swipe/scroll), then a rolagem ocorre de forma fluida.
- Given o seletor de mês, when o usuário seleciona uma nova data ou clica no atalho de mês atual, then o grid re-renderiza as 24 colunas a partir do novo mês.

## Verification

**Commands:**
- `npm run build` -- expected: O projeto compila sem erros TypeScript (no `frontend`).

## Suggested Review Order

**Entry Point (Page & Route)**

- Nova rota default do painel com injeção de dependências
  [`App.tsx:84`](../../frontend/src/App.tsx#L84)

- Layout orquestrador, mantendo o height fluido sem invadir as áreas do header
  [`ConsolidacaoPage.tsx:5`](../../frontend/src/pages/Consolidacao/ConsolidacaoPage.tsx#L5)

**Navegação Temporal (Toolbar)**

- Integração bidirecional do controle de período com o `monthStore` global e navegação local
  [`ConsolidacaoToolbar.tsx:20`](../../frontend/src/components/Consolidacao/ConsolidacaoToolbar.tsx#L20)

**A Matriz de Consolidação (Grid)**

- Função utilitária de geração das labels e payloads para as próximas 24 colunas
  [`ConsolidacaoGrid.tsx:5`](../../frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx#L5)

- Semântica de tabela com headers e colunas pegajosas (`sticky`), usando separação de bordas correta
  [`ConsolidacaoGrid.tsx:32`](../../frontend/src/components/Consolidacao/ConsolidacaoGrid.tsx#L32)
