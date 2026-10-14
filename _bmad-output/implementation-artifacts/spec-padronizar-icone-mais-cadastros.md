---
title: 'Padronizar ícone de adição em Contas e Categorias'
type: 'bugfix'
created: '2026-10-14'
status: 'done'
route: 'one-shot'
---

# Padronizar ícone de adição em Contas e Categorias

## Intent

**Problem:** Os botões superiores Nova Conta e Nova Categoria usavam um sinal textual pequeno; os botões centrais do estado vazio não tinham ícone. Os cadastros de dependências nas abas financeiras já usavam um SVG maior.

**Approach:** Extrair o SVG existente para PlusIcon (20 px, decorativo, sem encolhimento) e reutilizar nos quatro botões de Contas e Categorias e nos links originais de dependências. EmptyState recebe um ícone opcional na ação. Os testes existentes de Contas passam a localizar o nome acessível sem sinal textual. Build, lint e 18 testes existentes passaram. Revisão independente sem regressões; nenhuma correção adicional ou pendência. Sem validação visual no navegador.

## Suggested Review Order

- Centraliza o mesmo desenho de adição em 20 px.
  [PlusIcon.tsx:1](../../frontend-web/src/components/ui/PlusIcon.tsx#L1)

- Aplica o ícone no cabeçalho e no botão central de Contas.
  [ContasPage.tsx:45](../../frontend-web/src/pages/Contas/ContasPage.tsx#L45)

- Aplica o ícone no cabeçalho e no botão central de Categorias.
  [CategoriasPage.tsx:45](../../frontend-web/src/pages/Categorias/CategoriasPage.tsx#L45)

- Permite ícone opcional sem alterar ações existentes.
  [EmptyState.tsx:58](../../frontend-web/src/components/ui/EmptyState.tsx#L58)

- Confirma abertura do formulário pelos dois botões após atualização do nome acessível.
  [ContasPage.test.tsx:84](../../frontend-web/src/pages/Contas/ContasPage.test.tsx#L84)
