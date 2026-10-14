---
title: 'Padronizar verde das ações de receitas'
type: 'bugfix'
created: '2026-10-14'
status: 'done'
route: 'one-shot'
---

# Padronizar verde das ações de receitas

## Intent

**Problem:** Cadastrar Conta e Cadastrar Categoria usavam green-600 nas receitas, diferente do primary (#004d40) da navegação.

**Approach:** Aplicar primary, primary-light no hover e primary no foco no ramo de receitas de RequiresDependencies, incluindo Tentar novamente. Build, lint e 12 testes existentes de Receitas Variáveis passaram. Sem validação visual no navegador. Revisão: nenhum defeito introduzido; seis grupos de problemas preexistentes registrados no ledger, quatro sugestões rejeitadas por não justificarem mudança neste ajuste visual.

Levantamento para análise: seletores de mês de Receitas Variáveis usam emerald-600. Contas e Categorias usam azul na criação e primary em Salvar. DatePicker contém um tema esmeralda sem uso atual nas receitas. Consolidação usa verdes semânticos para valores e fundos, sem indicar necessariamente inconsistência em ações.

## Suggested Review Order

- Alinha cadastro e recuperação ao verde da navegação nas duas abas de receitas.
  [RequiresDependencies.tsx:39](../../frontend-web/src/components/RequiresDependencies.tsx#L39)

- Comparar seletores esmeralda com ações de receitas.
  [MonthPicker.tsx:156](../../frontend-web/src/components/ui/MonthPicker.tsx#L156)

- Comparar criação azul com Salvar verde em Contas.
  [ContaFormModal.tsx:112](../../frontend-web/src/pages/Contas/components/ContaFormModal.tsx#L112)

- Comparar criação azul com Salvar verde em Categorias.
  [CategoriaFormModal.tsx:119](../../frontend-web/src/pages/Categorias/components/CategoriaFormModal.tsx#L119)
