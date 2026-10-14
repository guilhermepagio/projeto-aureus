---
title: 'Suavizar vermelho das abas de despesas'
type: 'chore'
created: '2026-10-14'
status: 'done'
route: 'one-shot'
---

# Suavizar vermelho das abas de despesas

## Intent

**Problem:** O vermelho das abas de despesas e dos botões Cadastrar Conta e Cadastrar Categoria no sistema vazio estava muito vibrante.

**Approach:** Aplicar vinho rose-800 às abas de despesas ativas, no desktop e no mobile, e às ações de cadastro nas rotas de despesas sem dependências. Usar rose-900 no hover e rose-800 no foco por teclado. Build, lint e os 180 testes existentes passaram. O CSS de produção contém as novas classes. Sem inspeção visual no navegador.

Revisão independente: uma correção aplicada para preservar o hover vinho no desktop; nove apontamentos rejeitados por ampliarem o escopo ou proporem abstrações e testes desnecessários para esta mudança visual pontual. Nenhuma pendência.

## Suggested Review Order

- Aplica vinho às duas abas de despesas, incluindo navegação mobile e foco.
  [Navigation.tsx:16](../../../../../frontend-web/src/components/Navigation/Navigation.tsx#L16)

- Alinha os cadastros de dependências ao vinho das abas de despesas.
  [RequiresDependencies.tsx:32](../../../../../frontend-web/src/components/RequiresDependencies.tsx#L32)
