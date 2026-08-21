---
title: 'Divisão clara entre colunas nas listagens'
type: 'spec'
created: '2026-08-21'
status: 'done'
route: 'one-shot'
---

# Divisão clara entre colunas nas listagens

## Intent
**Problem:** O usuário sentia falta de uma divisão clara nas colunas para saber onde cada uma começava e terminava nas tabelas de listagem.
**Approach:** Adicionados utilitários `divide-x` e `divide-gray-200` nas linhas de cabeçalho (`<thead> <tr>`) e de dados (`<tbody> <tr>`) em todas as 6 páginas de listagem, além de remover a borda esquerda hardcoded da coluna de ações.

## Suggested Review Order

1. [CategoriasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/Categorias/CategoriasPage.tsx) — Adição de `divide-x divide-gray-200`
2. [ContasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/Contas/ContasPage.tsx) — Adição de `divide-x divide-gray-200`
3. [DespesasFixasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx) — Adição de `divide-x divide-gray-200`
4. [DespesasVariaveisPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx) — Adição de `divide-x divide-gray-200`
5. [ReceitasFixasPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx) — Adição de `divide-x divide-gray-200`
6. [ReceitasVariaveisPage.tsx](file:///home/guilhermepagio/developer/workspace/projeto-aureus/frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx) — Adição de `divide-x divide-gray-200`
