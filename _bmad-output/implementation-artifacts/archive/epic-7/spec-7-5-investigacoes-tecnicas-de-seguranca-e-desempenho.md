---
title: 'Story 7.5: Investigações técnicas de segurança e desempenho'
type: 'chore'
created: '2026-10-25'
status: 'done'
baseline_commit: 'c389e76e518d66099fef384448ec27b7d1800233'
review_loop_iteration: 0
context:
  - '_bmad-output/specs/spec-epico-7/seguranca.md'
  - '_bmad-output/specs/spec-epico-7/desempenho.md'
  - '_bmad-output/specs/spec-epico-7/rastreabilidade.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Faltam evidências reproduzíveis para decidir revogação JWT, ausência de tenant e custos das consultas.

**Approach:** Caracterizar o comportamento vigente e medir cargas sintéticas em PostgreSQL descartável, entregando relatório e recomendações.

## Boundaries & Constraints

**Always:** CAP-4 investigativo, CAP-6/7/9; DW-2/160/139/117/147/153; DW-38 consolidado; A10. Preservar isolamento, precisão monetária, 24 meses e saldo pré-grade. Separar observação, hipótese e decisão.

**Ask First:** Mudanças de produção, uso de dados reais ou encerramento/redução do compromisso A10.

**Never:** Implementar revogação/renovação, tabela de sessões, troca do fallback `public`, paginação, JOIN FETCH, SQL agregado ou migrações. Recriar o coletor removido da 7.4. Declarar investigação como correção.

## I/O & Edge-Case Matrix

| Cenário | Entrada | Evidência | Proteção |
|---|---|---|---|
| Sessão | JWT copiado; logout com CSRF; expiração; falha de rede | Replay e janela residual; limpeza local versus servidor | Nenhum token nos artefatos |
| Tenant | A/B/public; contexto nulo/vazio/branco; sessão existente/nova | Consultas, ID, escrita e bootstrap reais | Transações isoladas; limpeza em finally |
| Dados | Relações compartilhadas/distintas; histórico anterior; tenant vazio | SQL, volume, custo e equivalência financeira | Seletores completos; 403/404; rejeição sem mutação |

</frozen-after-approval>

## Code Map

Base Java: `backend-api/src/main/java/com/guilhermepagio/aureus/backend/`.
- `security/{JwtUtil,JwtAuthenticationFilter,CurrentTenantIdentifierResolverImpl}.java` — assinatura/expiração, contexto e fallback; `controller/AuthController.java:48` apenas expira cookie. TTL configurado: 24h.
- `security/OAuth2LoginSuccessHandler.java:51` persiste Usuario global sem tenant; `domain/TenantAwareEntity.java:12` usa @TenantId. Resolver configurado por classe em `backend-api/src/main/resources/application.yaml:14`.
- `service/ConsolidacaoService.java:55/252` — cinco/três leituras, agregação em memória; saldo pré-grade no retorno por conta.
- `service/DespesaVariavelService.java:36/57` e demais famílias — DTO completo e proprietário→findById; `domain/dto/DespesaVariavelResponseDTO.java:34` lê relações LAZY incluindo descrição/observações.
- Testes: `backend-api/src/test/java/com/guilhermepagio/aureus/backend/`; reutilizar `AbstractIntegrationTest.java`, `integration/IndexMigrationIT.java` para bootstrap e `service/ConsolidacaoServiceTest.java` para exemplos financeiros. Reusar SecurityIT/MultiTenancyIT.

## Tasks & Acceptance

**Execution:**
- [x] `_bmad-output/implementation-artifacts/7-5-evidencias-investigacoes.md` — registrar protocolo Q-4 abaixo antes da coleta; depois consolidar resultados, alternativas/custos de revogação, riscos, limites e encaminhamento por DW.
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/SecurityInvestigationIT.java` — caracterizar replay/logout/expiração; acesso e persistência sem tenant, inclusive ID e SQL nativo de proprietário; observar resolver efetivo, bootstrap Flyway/JPA e OAuth sintético. Distinguir caminhos internos de exposição HTTP e sessão existente de nova.
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/PerformanceInvestigationIT.java` — medir consolidação, seis listagens serializadas, DTOs LAZY e, por último, titularidade. Validar oráculo financeiro independente, cardinalidade completa dos seletores e contratos da matriz. Instrumentação exclusiva de teste; sessões frescas e banco próprio.
- [x] `_bmad-output/implementation-artifacts/7-5-investigation-evidence/measurement.json` — publicar amostras, SQL normalizado sem valores, ambiente/digest, revisão Git completa, hashes, comandos e verificações; preservar evidência anterior se coleta falhar.
- [x] `_bmad-output/implementation-artifacts/deferred-work.md` e `sprint-status.yaml` no mesmo diretório — atualizar seis DW e A10 com data ISO, resultado, evidências e pendências; A10 permanece aberto. Registros preenchidos após os testes; três camadas do step-04 concluídas, nove patches aplicados e conferência final do agente principal concluída em 2026-10-25; nenhum encerramento antecipado.

**Acceptance Criteria:**
- Given SEG-12/13/17/18, when os cenários forem executados, then o relatório distinguirá riscos demonstrados, bootstrap legítimo e alternativas de revogação sem alterar produção.
- Given PERF-01–06/12–14, when a coleta terminar, then haverá métricas por operação/carga e equivalência dos 24 meses/histórico, com recomendações condicionadas à evidência.
- Given revisão concluída, when a história chegar a done, then os registros refletirão investigação atendida ou escopo residual aberto, sem encerrar A10 como implementação.

## Spec Change Log

## Design Notes

Q-4: Java 21, PostgreSQL 16 descartável, três tenants; 100/1.000/10.000 movimentos por família e 10/100/1.000 contas/categorias por tenant. Janela 2026-01–2027-12; dados determinísticos 2021–2028, parcelas cruzando bordas, fixos e vínculos opcionais. N+1: 10/100/1.000 movimentos, pares compartilhados versus distintos.

Coleta sequencial: uma amostra fria, três aquecimentos e dez amostras com sessões frescas. Medir tempo, bytes UTF-8, SQL preparado, entidades carregadas, alocação e heap observado, separando fixture/validação do intervalo. MockMvc não mede rede. Registrar mediana/máximo; heap observado não equivale a pico.

Sinais exploratórios: mediana ≥200ms, resposta ≥1MiB, alocação ≥32MiB ou SQL crescendo com relações distintas. Não são SLOs aprovados nem autorização de otimizar. Medir titularidade separada e como fração do total.

## Verification

Em `backend-api/`, JDK 21 e Docker:
- `./mvnw.cmd verify -Pintegration-test -DforkCount=1 -DreuseForks=true "-Dit.test=SecurityInvestigationIT,PerformanceInvestigationIT"` — cenários e nova coleta em `target/7-5-investigation-evidence/measurement.json`.
- Mesmo comando focado com `-Dinvestigation.publish=true` — publicação explícita autorizada em `_bmad-output`, sem sidecar; relatório/verificação devem acompanhar o novo JSON.
- `./mvnw.cmd verify -Pintegration-test -DforkCount=1 -DreuseForks=true` — regressão; preserva o JSON arquivado.

Na raiz: `git diff --check`; conferir hashes, artefatos reproduzíveis e ausência de alterações em produção.

### Execução verificada — 2026-10-25

- Comando focado autorizado após os patches: **225 unitários + 2 investigativos**; regressão normal das mesmas fontes: **225 unitários + 72 integrações**, sem falhas, erros ou skips. Inclui 6 testes novos de publicação/ciclo de vida.
- Limpeza local em sucesso/falha de rede: `npm test -- src/components/Header/Header.test.tsx`, **3 aprovados**; nenhuma alteração no frontend.
- 32 séries com fria + 3 aquecimentos + 10 amostras, sessões frescas e SQL real; oráculo em centavos para 24 meses e saldo pré-grade; dados exclusivamente sintéticos em banco próprio.
- [Relatório](7-5-evidencias-investigacoes.md), [amostras](7-5-investigation-evidence/measurement.json) e [verificação](7-5-investigation-evidence/verification.json): 74 hashes de fontes conferidos; JSON arquivado SHA-256 `2b5ca159cd4154dd9f8ac8a2d105ede9eda86c566447d2302bbac14b84bdcbf8`; sem mudanças em produção; `git diff --check` aprovado.
- Replay após logout 200 e expirado 401; fallback novo mantém public sem ler ID alheio, mas atribui escrita interna a public; sessão existente conserva tenant da abertura. Bootstrap/OAuth global legítimos distinguidos de acesso HTTP.
- N+1 demonstrado: 3 SQL com pares compartilhados e 21/201/2.001 com pares distintos. Custos, alternativas e limites constam no relatório; nenhuma correção de produção implementada.
- **Nove patches conferidos pelo agente principal em 2026-10-25.** Seis DW e A10 continuam open pelas decisões/resíduos explicitados; investigação verificada não entrega a otimização de A10.

## Review Findings

2026-10-25: revisão independente do step-04 concluída em três camadas pelo agente principal. Os nove itens abaixo foram triados como patches, sem mudança de intenção ou produção. Patches aplicados em lote; foco autorizado e regressão concluídos com sucesso, 74 hashes conferidos, SHA arquivado preservado na regressão e **conferência final pelo agente principal concluída em 2026-10-25**. Nenhum outro achado aceito ou deferido.

| ID | Patch aplicado | Evidência de verificação |
|---|---|---|
| RF-1 | Publicação com um único move atômico do JSON; sidecar removido e nenhuma operação posterior de I/O no publicador. | InvestigationEvidenceTest: sucesso, falha pré-commit preservando bytes e JSON inválido. |
| RF-2 | Store raiz do JUnit com runId próprio, descarte em falha/fim e falha explícita para seções incompletas; JVM única reutilizada exigida. | InvestigationEvidenceTest: runs independentes, incompletude, descarte em falha e configuração de forks. |
| RF-3 | Execução normal escreve em target; arquivamento exige flag explícita; comandos documentados. | Coleta autorizada e regressão normal têm runIds distintos e SHA arquivado preservado, conferidos em verification.json. |
| RF-4 | Assertiva de ID alheio ausente nos três contextos nulo/vazio/branco. | SecurityInvestigationIT, além do registro da observação. |
| RF-5 | Snapshot das seis tabelas financeiras antes/depois do OAuth; Usuario global permanece permitido. | SecurityInvestigationIT. |
| RF-6 | Conjuntos exatos de IDs e rejeição de duplicatas em listagens e linhas não zeradas do oráculo. | PerformanceInvestigationIT, preservando validação monetária. |
| RF-7 | Snapshot das seis tabelas financeiras em todas as rejeições 403/404. | PerformanceInvestigationIT, fora da medição. |
| RF-8 | Operação/razão renomeadas para criação revertida, explicitando ausência de COMMIT durável. | JSON e relatório; razão de execuções separadas preservada. |
| RF-9 | Tempo/alocação incluem regex/mapas do SQL inspector, com overhead não quantificado declarado. | Limites do relatório e measurementBoundaries no JSON. |

### Code review fracionado — 2026-10-25

Revisão de `e7738c9f2b24ea133fccd8149bcbf47049ec6021` contra o baseline `c389e76e518d66099fef384448ec27b7d1800233`, em três grupos. Coleta/publicação/integridade, segurança/tenant e medições/oráculo/rastreabilidade passaram pelas quatro camadas de revisão, sem achados relevantes. Resultado: 0 decisões, 0 patches, 0 adiamentos e 0 descartes. As tentativas interrompidas por limite de uso foram refeitas e todas as camadas concluíram.

Conferidos o SHA do JSON arquivado, 74 fontes, 32 séries e a coerência entre código, dados e relatório. A análise reutilizou as execuções aprovadas das mesmas fontes; nenhum teste foi reexecutado nesta revisão. Sprint atualizado para `done`; seis DW e A10 preservam os resíduos/decisões explicitados. Registro em [verification.json](7-5-investigation-evidence/verification.json).

## Suggested Review Order

**Resultados e limites**

- Leia os resultados e limites antes de interpretar os custos.
  [7-5-evidencias-investigacoes.md:19](7-5-evidencias-investigacoes.md#L19)

**Sessão e isolamento**

- Distingue remoção do cookie, replay da cópia e expiração.
  [SecurityInvestigationIT.java:70](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/SecurityInvestigationIT.java#L70)

- Confere acesso por ID, escrita sem contexto e consulta nativa de proprietário.
  [SecurityInvestigationIT.java:110](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/SecurityInvestigationIT.java#L110)

**Medições e equivalência**

- Executa cargas, listagens completas, experimento LAZY e validação de titularidade.
  [PerformanceInvestigationIT.java:54](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/PerformanceInvestigationIT.java#L54)

- Calcula referência financeira independente em centavos, incluindo histórico anterior à grade.
  [PerformanceInvestigationIT.java:359](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/PerformanceInvestigationIT.java#L359)

**Publicação das evidências**

- Vincula seções à execução JUnit e rejeita coleta incompleta ou divergente.
  [InvestigationEvidence.java:53](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/InvestigationEvidence.java#L53)

- Valida o arquivo temporário antes do único commit atômico.
  [InvestigationEvidence.java:28](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/InvestigationEvidence.java#L28)

**Verificação e rastreabilidade**

- Registra testes, revisão, hashes e preservação do arquivo arquivado.
  [verification.json:1](7-5-investigation-evidence/verification.json#L1)

- Mantém resultados e decisões futuras dos seis registros investigativos.
  [deferred-work.md:251](deferred-work.md#L251)

- Preserva A10 aberto até implementação verificada ou decisão explícita de escopo.
  [sprint-status.yaml:88](sprint-status.yaml#L88)

- Testa preservação na falha e isolamento de execuções.
  [InvestigationEvidenceTest.java:32](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/investigation/InvestigationEvidenceTest.java#L32)
