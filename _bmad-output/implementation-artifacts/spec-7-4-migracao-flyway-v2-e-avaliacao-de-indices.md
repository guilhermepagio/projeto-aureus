---
title: 'Story 7.4: Migração Flyway V2 e avaliação de índices'
type: 'chore'
created: '2026-10-24'
status: 'done'
baseline_commit: 'dc416175d794645372abf9331201868e76ab6a90'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-7-context.md'
  - '_bmad-output/specs/spec-epico-7/desempenho.md'
  - '_bmad-output/specs/spec-epico-7/rastreabilidade.md'
  - 'docs/architecture/testing.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Faltam índices de tenant em contas/categorias; as demais alterações dependem de investigação.

**Approach:** Entregar Flyway V2, avaliar candidatos em PostgreSQL descartável e remover somente redundância comprovada, preservando unicidade e integridade.

## Boundaries & Constraints

**Always:** CAP-8; DW-155/DW-161; action item A13. Preservar V1 byte a byte. Criar `idx_contas_usuario_id` e `idx_categorias_usuario_id`. Inspecionar definição, validade e dependências no catálogo antes de remover índices. Preservar UNIQUE e alinhar os mapeamentos JPA quando uma remoção for comprovada, evitando recriação por `ddl-auto: update`.

**Ask First:** Mudanças fora do contrato CAP-8 ou execução em banco com dados reais.

**Never:** Duplicar índice simples de usuario_id nas quatro movimentações; alterar consultas, contratos HTTP ou resultados financeiros; presumir ganho ou aplicar alteração investigativa sem evidência e decisão registradas. Não criar/rodar migração durante este planejamento.

## I/O & Edge-Case Matrix

| Cenário | Estado | Resultado | Proteção |
|---|---|---|---|
| Base limpa ou V1 existente | Dados e histórico Flyway preservados | V2 aplicada, índices obrigatórios presentes | Validar checksum e reaplicação sem pendências |
| Redundância confirmada | Secundário equivalente, independente de constraint; UNIQUE válida | Remover somente secundário | Duplicata continua rejeitada, inclusive após bootstrap JPA |
| Redundância não confirmada | Índice ausente, diferente ou dependência impeditiva | Preservar índice/constraint e registrar pendência | Não declarar correção entregue |

</frozen-after-approval>

## Code Map

- `backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql` — imutável: tabelas, UNIQUE e índices.
- `backend-api/src/main/resources/application.yaml` — Flyway antes de JPA; `ddl-auto: update`.
- Sob `backend-api/src/main/java/com/guilhermepagio/aureus/backend/`: `domain/Usuario.java:17` declara secundário em `@Table` e UNIQUE em `googleSubjectId`; as quatro entidades de movimentações também declaram índices temporais. `domain/TenantAwareEntity.java:12` define `@TenantId`.
- No mesmo pacote: `service/ContaService.java:60` e `CategoriaService.java:60` excluem com flush, protegidos pelas FKs; não há sondagens `existsByConta/Categoria`. Repositórios financeiros listam por tenant/descrição; `service/ConsolidacaoService.java:65` agrega datas em memória.
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java` — referência PostgreSQL `16-alpine`; `BackendApplicationIT.java` verifica Flyway no bootstrap. `backend-api/pom.xml` separa `*IT` via Failsafe/perfil `integration-test`.

## Tasks & Acceptance

**Execution:**
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/IndexMigrationIT.java` — testes inicialmente vermelhos para matriz, catálogo das seis tabelas, dados/UNIQUE/FKs, ausência de duplicação e bootstrap JPA. Isolar banco/esquemas da suíte compartilhada.
- [x] `_bmad-output/implementation-artifacts/7-4-evidencias-indices.md` — registrar ambiente, revisão, catálogo antes/depois e procedimento reproduzível. Definir carga sintética, distribuição/seletividade e critério de custo antes das medições; comparar planos `EXPLAIN (ANALYZE, BUFFERS)`, leitura, escrita e espaço para oito FKs e quatro índices temporais. Medir vínculos presentes/ausentes e DELETE com rollback; distinguir SQL real de sondagens hipotéticas. Decidir cada candidato, registrando limites da amostra.
- [x] `backend-api/src/main/resources/db/migration/V2__tenant_indexes_and_redundancy.sql` — implementar índices obrigatórios e apenas decisões sustentadas pela investigação; condicionar remoção do secundário de usuarios à verificação no próprio banco alvo.
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java` e entidades de movimentações — retirar somente declarações dos índices cuja remoção foi comprovada; manter UNIQUE e demais mapeamentos. Tornar os testes verdes.
- [x] `_bmad-output/implementation-artifacts/deferred-work.md` e `sprint-status.yaml` no mesmo diretório — atualizar DW-155/DW-161 e A13 com data ISO, evidências, revisão e pendências antes de done; manter abertos os recortes parciais.

**Acceptance Criteria:**
- Given seis tabelas com filtro de tenant, when V2 e JPA terminarem, then cada tabela terá índice válido liderado por usuario_id, mantendo os compostos da V1 e seu checksum.
- Given os doze candidatos, when a avaliação terminar, then cada um terá catálogo, planos, custos e recomendação rastreáveis.
- Given testes e revisão concluídos, when a história mudar para done, then DW-155/DW-161 e A13 refletirão resultado integral ou pendências explícitas conforme o protocolo.

## Spec Change Log

## Verification

Com JDK 21 e Docker ativo, em `backend-api/`:
- `./mvnw.cmd verify -Pintegration-test -Dit.test=IndexMigrationIT` — matriz aprovada e migração válida.
- `./mvnw.cmd verify -Pintegration-test` — unitários e integração aprovados.

Na raiz: `git diff --check` — sem erros; comparar V1 com `baseline_commit` — conteúdo idêntico.

## Review Findings

Revisão de 2026-10-24 sobre o baseline `dc416175d794645372abf9331201868e76ab6a90` e as alterações locais desta entrega. Blind Hunter, Edge Case Hunter e Verification Gap Reviewer concluíram suas análises; 11 apontamentos acionáveis foram corrigidos, sem adiamentos ou decisões de escopo pendentes nesta história.

- [x] Exigir collation compatível nos índices de tenant e cobertura válida dos quatro compostos existentes; impedir sucesso da V2 em catálogo divergente.
- [x] Verificar índices obrigatórios inválidos/collation divergente e compostos ausentes/inválidos/incompatíveis, com rollback e histórico na V1.
- [x] Registrar inventário próprio das oito FKs e comparar integralmente definições/validade após V2 e bootstrap JPA.
- [x] Manter titularidade no UPDATE sintético e exigir SQLSTATE 23503 no DELETE rotulado como rejeitado.
- [x] Permitir reprodução por digest e publicar a coleta atomicamente, preservando a evidência anterior em falha.
- [x] Verificar quantidade_parcelas/data_fim na preservação dos dados e corrigir o vendor do JDK no relatório.

Os patches foram conferidos pelo agente principal; a coleta foi refeita com 12 candidatos, oito FKs preservadas e quatro lotes de 1.000 linhas sem vínculos cruzados. Os hashes de todas as fontes medidas/testadas conferem. Validação final: 219 unitários e 70 integrações, incluindo 33 cenários de migração, sem falhas, erros ou skips; foco de 219 unitários e 33 integrações também aprovado. V1 idêntica ao baseline; `git diff --check` aprovado. A matriz está coberta pelos testes de base limpa/upgrade, remoção comprovada com bootstrap e preservação dos catálogos sem comprovação. Evidências em [7-4-evidencias-indices.md](7-4-evidencias-indices.md) e [verification.json](7-4-index-evidence/verification.json).

DW-155 e DW-161 encerrados após a revisão do recorte aprovado. A13 permanece aberto: a recomendação condicional dos candidatos não entrega a adição histórica de índices de FK.

## Suggested Review Order

**Índices e condições de migração**

- Criar os dois índices obrigatórios e validar a cobertura de tenant antes de concluir.
  [V2__tenant_indexes_and_redundancy.sql:2](../../backend-api/src/main/resources/db/migration/V2__tenant_indexes_and_redundancy.sql#L2)

- Remover o secundário somente após comprovar redundância, unicidade e dependências no alvo.
  [V2__tenant_indexes_and_redundancy.sql:64](../../backend-api/src/main/resources/db/migration/V2__tenant_indexes_and_redundancy.sql#L64)

- Preservar UNIQUE sem recriar o secundário no bootstrap JPA.
  [Usuario.java:16](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java#L16)

**Medições e rastreabilidade**

- Comparar custos dos oito candidatos de FK e registrar a recomendação condicional.
  [7-4-evidencias-indices.md:40](7-4-evidencias-indices.md#L40)

- Reproduzir por digest e publicar evidência somente após sucesso completo.
  [evaluate-indexes.py:92](../../backend-api/scripts/evaluate-indexes.py#L92)

- Registrar a conclusão da implementação condicional e a preservação de UNIQUE.
  [deferred-work.md:224](deferred-work.md#L224)

- Manter aberto o compromisso histórico de adição de índices de FK.
  [sprint-status.yaml:107](sprint-status.yaml#L107)

**Testes de migração e integridade**

- Comprovar upgrade, histórico, dados e catálogo após bootstrap real do aplicativo.
  [IndexMigrationIT.java:76](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/IndexMigrationIT.java#L76)

- Recusar índices inutilizáveis e comprovar rollback sem avançar o histórico Flyway.
  [IndexMigrationIT.java:172](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/IndexMigrationIT.java#L172)

- Comparar as definições completas das oito FKs, além de sua contagem.
  [IndexMigrationIT.java:335](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/IndexMigrationIT.java#L335)
