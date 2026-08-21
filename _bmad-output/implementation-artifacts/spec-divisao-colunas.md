---
title: 'Modernização e Padronização das Listagens'
type: 'spec'
created: '2026-08-21'
status: 'done'
route: 'one-shot'
---

# Modernização e Padronização das Listagens

## Intent
**Problem:** O usuário sentia falta de uma divisão clara nas colunas para saber onde cada uma começava e terminava nas tabelas de listagem, além de apontar problemas com botões volumosos na coluna de ações, desalinhamento de conteúdo e despadronização nas larguras e alturas das linhas e cabeçalhos.
**Approach:** 
1. Adicionados utilitários `divide-x` e `divide-gray-200` nas linhas de cabeçalho e dados de todas as 6 páginas.
2. Refatoração dos botões de ação para um `<ActionMenu>` suspenso via `createPortal`, abrindo espaço útil para as colunas.
3. Centralização de todos os conteúdos numéricos e datas, com ajustes estritos de paddings (`px-3` para `px-1` e redistribuição de larguras).
4. Equalização total de altura (`h-[44px]` nos cabeçalhos, e `py-0.5` + `truncate` nas linhas) garantindo simetria absoluta entre as tabelas do sistema.

## Suggested Review Order

1. [ActionMenu.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/components/ui/ActionMenu.tsx) — Componente de menu suspenso centralizado via Portal para z-index absoluto.
2. [CategoriasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/Categorias/CategoriasPage.tsx) — Aplicação de divisórias, ActionMenu, equalização de paddings e alturas fixas.
3. [ContasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/Contas/ContasPage.tsx) — Aplicação de divisórias, ActionMenu, equalização de paddings e alturas fixas.
4. [DespesasFixasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx) — Aplicação de divisórias, ActionMenu, equalização de paddings e alturas fixas.
5. [DespesasVariaveisPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx) — Aplicação de divisórias, ActionMenu, equalização de paddings e alturas fixas.
6. [ReceitasFixasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx) — Aplicação de divisórias, ActionMenu, equalização de paddings e alturas fixas.
7. [ReceitasVariaveisPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx) — Aplicação de divisórias, ActionMenu, equalização de paddings e alturas fixas.
