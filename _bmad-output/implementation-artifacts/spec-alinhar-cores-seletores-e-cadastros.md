---
title: 'Alinhar cores dos seletores e cadastros'
type: 'bugfix'
created: '2026-10-14'
status: 'done'
route: 'one-shot'
---

# Alinhar cores dos seletores e cadastros

## Intent

**Problem:** Seletores de mês de receitas usavam esmeralda, diferente do verde primary da navegação e das ações. Contas e Categorias tinham criação azul e submissão verde.

**Approach:** Aplicar primary aos temas verdes dos seletores de mês e data, inclusive foco, ícones, seleção, atalhos e fundos suaves derivados do token. Aplicar azul aos botões Salvar e ao foco dos campos de Contas e Categorias. Incluir indicador azul de foco por teclado nos botões Salvar. Resolve os três pontos levantados no registro anterior; verdes semânticos de valores e fundos da Consolidação permanecem com sua função financeira. Build, lint e 45 testes existentes passaram. Revisão independente: nenhuma regressão concreta; nenhuma correção adicional ou pendência. Sem inspeção visual no navegador.

## Suggested Review Order

- Alinha o seletor de mês à identidade verde das receitas.
  [MonthPicker.tsx:152](../../frontend-web/src/components/ui/MonthPicker.tsx#L152)

- Mantém o tema verde do seletor de datas coerente com o de meses.
  [DatePicker.tsx:189](../../frontend-web/src/components/ui/DatePicker.tsx#L189)

- Alinha submissão e foco dos campos ao azul de Contas.
  [ContaFormModal.tsx:112](../../frontend-web/src/pages/Contas/components/ContaFormModal.tsx#L112)

- Alinha submissão e foco dos campos ao azul de Categorias.
  [CategoriaFormModal.tsx:119](../../frontend-web/src/pages/Categorias/components/CategoriaFormModal.tsx#L119)
