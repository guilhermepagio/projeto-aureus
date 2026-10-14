---
title: 'Corrigir avisos de null safety em referências de método'
type: 'bugfix'
created: '2026-10-14'
status: 'done'
route: 'one-shot'
---

# Corrigir avisos de null safety em referências de método

## Intent

**Problem:** O analisador Java do editor aponta conversões não verificadas do receptor em uma referência Usuario::getFotoPerfil e quatro referências BigDecimal::add.

**Approach:** Substituir apenas essas cinco referências por lambdas com parâmetros explícitos e final, preservando comportamento e alterações locais anteriores. Compilação limpa em Java 21 e 205 testes concluídos sem falhas. Revisão independente sem achados acionáveis; confirmação dos diagnósticos do editor depende da reanálise pelo JDT. Nenhum commit criado para evitar incluir alterações anteriores nos mesmos arquivos.

## Suggested Review Order

- Conferir o acesso à foto dentro de Optional.map.
  [AuthController.java:37](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java#L37)

- Conferir as quatro reduções dos valores históricos com a mesma soma e identidade.
  [ConsolidacaoService.java:184](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java#L184)
