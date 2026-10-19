# Escopo aprovado e protocolo de conclusão

Companion de `SPEC.md`. Decisões de planejamento de 2026-10-14 complementadas por Guilherme em 2026-10-19; nenhuma execução concluída por esta especificação.

## Autoridade e branch

O [relatório de triagem](../../planning-artifacts/backlog-triage-2026-10-14.md), nas seções **Validação interativa**, **Resultado final da validação interativa** e **Conferência final**, contém o corte base aprovado por Guilherme em 2026-10-14, complementado em 2026-10-19 com: (1) obrigatoriedade de índice para `usuario_id` em todas as tabelas filtradas por `TenantContext` via migração incremental Flyway V2 (`CAP-8` / `DW-155` / `A13`), e (2) inclusão da capacidade de observabilidade e logs estruturados (`CAP-10`).

O [ledger DW](../../implementation-artifacts/deferred-work.md) e [sprint-status.yaml](../../implementation-artifacts/sprint-status.yaml), especialmente `backlog_triage.validation_result` e `action_items`, confirmam os vínculos e são os registros operacionais a atualizar durante a execução. Não são companions adicionais nem autorização para ampliar escopo fora deste contrato.

A branch `planning/epico-7` foi criada/ativada antes das escritas, a partir da `main` limpa em `1d28a33`, que contém a organização versionada em `4cba299`. Por instrução atual de Guilherme, substitui `planning/epico-seguranca-desempenho` na política ativa; menções anteriores no relatório permanecem históricas.

## Matriz canônica de trabalho

Cada linha abaixo corresponde a um DW aberto ou capacidade adicionada em planejamento, não a uma história criada. Os IDs D mantêm a origem da triagem; os action items associados não geram tarefas independentes.

| DW / Escopo | Origem | Encaminhamento aprovado | CAP | Action item | Critérios |
|---|---|---|---|---|---|
| DW-13 | D013 | Planejar restrição de autorização e política pública/Actuator | CAP-1 | Nenhum | SEG-01–03 |
| DW-14 | D014 | Planejar verificação de segurança/expiração do cookie de logout | CAP-2 | Nenhum | SEG-05–06 |
| DW-26 | D026 | Planejar verificação do logout/limpeza local, inclusive erro de rede | CAP-3 | Nenhum | SEG-07, SEG-09 |
| DW-35 | D035 | Planejar verificação de população/limpeza dos contextos | CAP-4 | Nenhum | SEG-10 |
| DW-36 | D036 | Planejar verificação de atributos/TTL do cookie OAuth | CAP-2 | Nenhum | SEG-04 |
| DW-37 | D037 | Planejar verificação do resolver, vinculada a DW-160 | CAP-4 | Nenhum | SEG-11 |
| DW-60 | D060 | Planejar tratamento de 401 durante uso e limpeza local | CAP-3 | Nenhum | SEG-08–09 |
| DW-176 | D176 | Planejar somente CSRF PUT/DELETE sem token e vínculos cruzados faltantes | CAP-5 | Nenhum | SEG-14–16 |
| DW-161 | D161 | Planejar condicionalmente; conferir catálogo e preservar UNIQUE | CAP-8 | A13 | PERF-09–11 |
| DW-2 | D002 | Investigar somente revogação antes da expiração | CAP-6 | Nenhum | SEG-17–18 |
| DW-160 | D160 + D038 | Investigar ausência de contexto, configuração e bootstrap | CAP-4 | Nenhum | SEG-12–13 |
| DW-117 | D117 | Investigar necessidade de paginação sem perder opções dos seletores | CAP-9 | Nenhum | PERF-01–03, PERF-12 |
| DW-139 | D139 | Investigar custo da consolidação, preservando saldo histórico | CAP-7 | A10 | PERF-01–06 |
| DW-147 | D147 | Investigar SQL real/N+1 no caminho completo dos DTOs | CAP-9 | Nenhum | PERF-01–03, PERF-13 |
| DW-153 | D153 | Investigar consultas de titularidade com prioridade menor | CAP-9 | Nenhum | PERF-01–03, PERF-14 |
| DW-155 | D155 | Criar índices obrigatórios `usuario_id` em `contas`/`categorias` (Flyway V2) + investigar FKs e índices de data sem tenant | CAP-8 | A13 | PERF-01–03, PERF-07–08 |
| Observabilidade | Direção 2026-10-19 | Planejar correlação `MDC` (`requestId` + `tenantId`) e logs estruturados sem dados sensíveis | CAP-10 | Nenhum | OBS-01–05 |

Total: **16 DW abertos** (8 de segurança, 1 condicional, 1 misto implementação/investigação `DW-155` e 6 investigações) mais **1 capacidade de observabilidade (`CAP-10`)**. A10 → DW-139; A13 → DW-155/DW-161.

## Origens sem tarefa independente

| Origem | Decisão preservada | Consequência |
|---|---|---|
| DW-38 / D038 | Consolidado em DW-160 | Configuração/ciclo de vida do resolver integra SEG-12–13; não criar outra tarefa nem marcar consolidação como correção entregue. |
| DW-63 / D063 | Descartado | Sem consumidor assíncrono identificado que justifique propagação de tenant. |
| DW-27 / D027 | Descartado | `save()` no login não comprova escrita SQL desnecessária; preservar atualização de perfil. |
| DW-28 / D028 | Descartado | `/me` na inicialização, sem polling/gargalo demonstrado; não adicionar cache de perfil neste corte. |
| DW-92 / D092 | Descartado | Cancelamento sem impacto relevante demonstrado; cancelamento no cliente não garante interrupção no servidor. |

Essas cinco origens mais os dezesseis DW abertos preservam as **21 decisões da triagem**, somadas à atualização de 2026-10-19. As demais origens D e os 13 action items históricos mantêm suas decisões de entrega, descarte, inaplicabilidade ou duplicação; não reabri-los por apontamentos amplos relacionados.

## Action items preservados

| ID de triagem | ID original no sprint | Compromisso e limite de encerramento |
|---|---|---|
| A10 | `epic-4-retro-item-10-otimizar-agregação-de-transações-no-back` | Otimizar agregação no backend substituindo carregamento em memória por queries SQL e índices. Hoje só a investigação DW-139 está aprovada; relatório não cumpre a implementação original. Fechar exige resolução integral verificada ou decisão explícita de mudança de escopo. |
| A13 | `epic-5-retro-item-13-criar-migração-flyway-v2-para-limpeza-de` | Migração Flyway V2 cobrindo os índices obrigatórios `usuario_id` em `contas` e `categorias`, a remoção condicional da redundância em `usuarios` (`DW-161`) e as decisões de índices de FKs (`DW-155`). Resolver apenas uma parte não fecha o outro DW nem A13. |

Preservar IDs originais, owners, refs, `status_at_triage` e histórico. Não interpretar o `done` histórico de A10 como entrega atual comprovada. Os dois action items permanecem `open` nesta etapa.

## Protocolo obrigatório antes de `done`

1. **Na definição da história:** declarar CAP, critérios de aceite, DW e action items correspondentes, distinguindo implementação, verificação, investigação e condição. Se não houver action item correspondente, registrar `nenhum`; não omitir a associação.
2. **Na verificação:** executar os testes aplicáveis e/ou medições/catálogo próprios do recorte; submeter o resultado à revisão. Registrar data ISO (`AAAA-MM-DD`), revisão/commit avaliado, artefatos/comandos, resultado e evidência da revisão. Para investigação, evidência é o relatório reproduzível e sua análise, sem alegação de código entregue.
3. **Antes de marcar a história `done`:** atualizar cada DW declarado em `deferred-work.md` e cada action item correspondente em `sprint-status.yaml` com data, evidência, resultado, estado e pendências. Atualizar ambos quando houver A10/A13; a evidência apenas no arquivo da história não satisfaz o requisito.
4. **Ao decidir encerramento:** fechar somente o registro cujo escopo inteiro foi atendido e verificado. Atendimento parcial mantém `open`, com progresso datado, escopo residual e próximo passo explícitos. Concluir investigação não encerra automaticamente uma correção ou compromisso de implementação. Um DW estritamente investigativo só pode encerrar quando todo seu escopo de investigação estiver atendido.
5. **Se a história concluir somente investigação ou parte do trabalho:** ela pode registrar esse resultado como concluído após atualizar os registros, mantendo DW/action items pendentes abertos. Alterar/reduzir o escopo do registro ou encerrar por inaplicabilidade exige decisão explícita, datada e fundamentada; não converter isso em correção implementada.

Registro mínimo, usando a estrutura nativa de cada arquivo e sem substituir os campos originais:

```yaml
data: AAAA-MM-DD
historia: <identificador da história futura>
resultado: <implementação verificada | verificação | investigação | parcial>
evidencia: <artefato/comando e resultado, com revisão ou commit>
revisao: <referência e conclusão da revisão>
pendencias: <escopo residual e próximo passo, ou nenhuma se integralmente resolvido>
estado_do_registro: <open enquanto parcial; done somente se integralmente resolvido>
data_da_resolucao: <preencher somente no encerramento integral>
```

## Limite desta entrega

Especificação e companions criados; política ativa de branch corrigida. Os 16 DW, A10/A13, 13 action items históricos e 35 entradas de `development_status` permanecem inalterados. Não há `stories.yaml`, histórias de execução, atualização de sprint, arquitetura nova, código, testes de aplicação, benchmark ou migração produzidos nesta etapa.
