---
title: 'Remover coletor de avaliação de índices'
type: 'chore'
created: '2026-10-25'
status: 'done'
route: 'one-shot'
---

# Remover coletor de avaliação de índices

## Intent

**Problem:** Guilherme solicitou a remoção do coletor de avaliação de índices da story 7.4.

**Approach:** Excluir `backend-api/scripts/evaluate-indexes.py` e ajustar suas referências operacionais na documentação. Preservar os JSONs como evidência histórica, as migrações e os testes. A busca por dependências e a revisão não encontraram consumidores operacionais remanescentes; `git diff --check` aprovado.

## Suggested Review Order

- Registrar a remoção e a revisão Git que conserva o coletor original.
  [7-4-evidencias-indices.md:11](7-4-evidencias-indices.md#L11)

- Direcionar a revisão da story para o procedimento histórico documentado.
  [spec-7-4-migracao-flyway-v2-e-avaliacao-de-indices.md:108](spec-7-4-migracao-flyway-v2-e-avaliacao-de-indices.md#L108)
