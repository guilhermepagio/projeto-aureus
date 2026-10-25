---
title: 'Story 7.2: Isolamento de Contexto, Correlação MDC e Logs Estruturados de Troubleshooting (Backend)'
type: 'feature'
created: '2026-10-19'
status: 'done'
baseline_commit: '00434e9978d132de880cb0874eaf9e5e77a3bf64'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-7-context.md'
  - '_bmad-output/specs/spec-epico-7/SPEC.md'
  - '_bmad-output/specs/spec-epico-7/seguranca.md'
  - '_bmad-output/specs/spec-epico-7/observabilidade.md'
  - '_bmad-output/specs/spec-epico-7/rastreabilidade.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O ciclo de vida de `TenantContext`, `SecurityContextHolder` (inclusive sob falha/exceção e reuso de thread — `DW-35`) e o contrato atual do `CurrentTenantIdentifierResolverImpl` (`DW-37`) carecem de verificação automatizada dedicada, enquanto filtros de segurança, autenticação, `GlobalExceptionHandler` e serviços financeiros não propagam correlação por requisição no `MDC` (`requestId`, `tenantId`) nem registram logs estruturados padronizados para troubleshooting (`CAP-10`).

**Approach:** Instrumentar `JwtAuthenticationFilter` para popular `MDC` (`requestId` curto e `tenantId` ou `"anon"`) e garantir limpeza conjunta de `MDC`, `TenantContext` e `SecurityContextHolder` em `finally`; configurar o pattern de console no `application.yaml`; adicionar logs estruturados SLF4J sem dados sensíveis em autenticação, `GlobalExceptionHandler` e serviços financeiros/consolidação; e cobrir com testes automatizados o contrato do resolver (`DW-37`), o ciclo de vida dos contextos/MDC (`DW-35`, `OBS-01..05`) e a ausência de vazamento de segredos ou valores financeiros nos logs.

## Boundaries & Constraints

**Always:**
- **Escopo rastreado:** `CAP-4 parcial` (`DW-35`, `DW-37`) e `CAP-10` (`OBS-01` a `OBS-05`) | **Action items:** `nenhum`.
- Preencher `MDC` com `requestId` (identificador curto por requisição) e `tenantId` (ID do usuário autenticado ou marcador explícito `"anon"` para requisições anônimas/rejeitadas) no início da requisição e remover/limpar integralmente em bloco `finally` junto com `TenantContext.clear()` e `SecurityContextHolder.clearContext()` (`OBS-01`, `OBS-02`, `DW-35`).
- Configurar o padrão de log do console em `application.yaml` exibindo `requestId` e `tenantId` do `MDC` usando apenas SLF4J/Logback nativo do Spring Boot (`OBS-03`).
- Registrar nos logs apenas metadados técnicos permitidos: rota/URI, método HTTP, status/código HTTP, tipo da exceção/motivo técnico, entidade, operação, IDs técnicos (`id`, `contaId`, `categoriaId`) e, na consolidação, contagem de itens e duração em ms (`OBS-04`).
- Preservar integralmente o contrato vigente de `CurrentTenantIdentifierResolverImpl` (`"public"` para nulo/vazio/espaços e `validateExistingCurrentSessions() == true`) ao testá-lo em `DW-37`.
- Atualizar `DW-35` e `DW-37` em `_bmad-output/implementation-artifacts/deferred-work.md` com data ISO, evidência de testes, revisão, pendências e estado antes de marcar a história como `done`.

**Ask First:**
- Qualquer alteração no fallback `"public"` do `CurrentTenantIdentifierResolverImpl` (que depende da investigação `DW-160` na Story 7.5) ou adição de dependências externas de telemetria.

**Never:**
- Proibido registrar em qualquer nível de log: tokens JWT, valores de cookies (`AUREUS_SESSION`, `XSRF-TOKEN`), valores monetários (`valor`, `valorParcela`, saldos), descrições/observações privadas de lançamentos, contas ou categorias, e dados pessoais de perfil (`email`, `nome`, `fotoPerfil`, `googleSubjectId`) (`OBS-04`).
- Não introduzir stack externa de observabilidade (Prometheus, Grafana, ELK, OpenTelemetry).
- Não inserir comentários no código-fonte com referências a épicos, stories ou IDs de backlog (`DW-*`).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Requisição autenticada válida (`SEG-10`, `OBS-01..02`) | Cookie `AUREUS_SESSION` com JWT válido para `user-1` | Durante a cadeia: `TenantContext=user-1`, `SecurityContext=user-1`, `MDC[requestId]` preenchido e `MDC[tenantId]=user-1`; após `finally`: ambos contextos e `MDC` limpos | N/A |
| Token inválido/expirado ou exceção na cadeia (`SEG-10`, `OBS-02`) | Cookie com JWT expirado/inválido ou `FilterChain` lançando exceção | Rejeição/limpeza de cookie e log `WARN`/`DEBUG` com rota e motivo técnico (sem expor token); após `finally`: `TenantContext`, `SecurityContextHolder` e `MDC` limpos | `finally` garante limpeza mesmo com exceção |
| Reuso de thread entre tenants e anônimo (`SEG-10`, `OBS-05`) | Mesma thread processa `user-1`, depois `user-2` e depois requisição anônima | `user-2` vê apenas `user-2`; chamada anônima vê `TenantContext=null`, `SecurityContext` vazio e `MDC[tenantId]=anon`, sem resíduo anterior | Isolamento por limpeza em `finally` |
| Resolver multi-tenant (`SEG-11` / `DW-37`) | `TenantContext` com `"user-42"`, `null`, `""` ou `"   "` | Retorna `"user-42"` quando preenchido e `"public"` para nulo/vazio/espaços; `validateExistingCurrentSessions()` retorna `true` | Contrato atual preservado |
| Logs de autenticação e erros (`OBS-01..05`) | Login OAuth, logout, erros `400`/`403`/`404`/`500` no `GlobalExceptionHandler` | `INFO` em login/logout; `WARN` em `400`/`403`/integridade; `DEBUG` em `404`; `ERROR` em `500` com método, URI, status e tipo/motivo, sem JWT, cookies, perfil ou valores financeiros | Sanitização estrita de mensagens |
| Logs de serviços e consolidação (`OBS-01..05`) | Criação/atualização/exclusão em `*Service` e cálculo em `ConsolidacaoService` | `INFO`/`DEBUG` em mutações com entidade, operação e IDs técnicos (`id`, `contaId`, `categoriaId`); `DEBUG` na consolidação com contagem de itens e duração em ms, sem descrições ou valores monetários | N/A |

</frozen-after-approval>

## Code Map

- `backend-api/src/main/resources/application.yaml` -- Configurar `logging.pattern.level` (ou `console`) para incluir `[req:%X{requestId:-} tenant:%X{tenantId:-}]` junto ao nível de log.
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java` -- Gerar `requestId` curto no início de `doFilterInternal`, definir `MDC.put("tenantId", "anon")` por padrão e atualizar para `subjectId` após validação do JWT; registrar log `DEBUG`/`WARN` em token ausente/inválido/expirado (com método, URI e motivo técnico, sem o valor do token/cookie); limpar `MDC.remove("requestId")` e `MDC.remove("tenantId")` no bloco `finally` junto com `TenantContext.clear()` e `SecurityContextHolder.clearContext()` (linhas 27–61).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java` -- Referência somente leitura (linhas 10–21) para verificação unitária do contrato `DW-37`.
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java` -- Adicionar log `INFO` ao concluir login OAuth com rota, resultado e `usuarioId` técnico, sem logar `email`, `nome`, `fotoPerfil`, `googleSubjectId` ou JWT (linhas 37–71).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java` -- Adicionar log `INFO` no encerramento de sessão (`POST /api/auth/logout`) e `DEBUG` em `/api/auth/me` não autenticado, contendo rota e resultado sem expor cookie (linhas 29–55).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java` -- Padronizar logs em todos os handlers com código HTTP, tipo da exceção/regra acionada, método e URI da requisição: `WARN` em violações de validação/domínio/integridade (`400`, `405`, `406`, `415`) e acesso negado (`403`), `DEBUG` em `404` (`ResourceNotFoundException`, `NoResourceFoundException`) e `ERROR` (com stack trace) em `500`, sem logar payloads ou valores rejeitados sensíveis (linhas 40–342).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java` -- Adicionar logs `INFO` em `criar`, `atualizar` e `excluir` registrando apenas entidade `Conta`, operação e `id` técnico (linhas 35–62).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java` -- Adicionar logs `INFO` em `criar`, `atualizar` e `excluir` registrando apenas entidade `Categoria`, operação e `id` técnico (linhas 35–62).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java` -- Adicionar logs `INFO` em `criar`, `atualizar` e `excluir` e `WARN` em rejeição de titularidade (`validarEObterConta`/`validarEObterCategoria`) com entidade e IDs técnicos (`id`, `contaId`, `categoriaId`), sem `descricao` ou `valor` (linhas 43–115).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java` -- Adicionar logs `INFO` em `criar`, `atualizar` e `excluir` e `WARN` em rejeição de titularidade com entidade e IDs técnicos (`id`, `contaId`, `categoriaId`), sem `descricao` ou `valor` (linhas 43–115).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java` -- Adicionar logs `INFO` em `criar`, `atualizar` e `excluir` e `WARN` em rejeição de titularidade com entidade e IDs técnicos (`id`, `contaId`, `categoriaId`), sem `descricao` ou `valorParcela` (linhas 52–132).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java` -- Adicionar logs `INFO` em `criar`, `atualizar` e `excluir` e `WARN` em rejeição de titularidade com entidade e IDs técnicos (`id`, `contaId`, `categoriaId`), sem `descricao` ou `valorParcela` (linhas 52–128).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Adicionar logs `DEBUG` em `calcularConsolidacaoPorConta` e `calcularConsolidacaoPorCategoria` registrando `mesAno`, contagens de itens processados/linhas retornadas e duração em ms, sem saldos ou valores monetários (linhas 51–171, 241–303).
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImplTest.java` -- Novo teste unitário verificando `resolveCurrentTenantIdentifier()` para tenant preenchido, `null`, `""`, `"   "` e `validateExistingCurrentSessions()` (`DW-37`).
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/ContextAndMdcLifecycleTest.java` -- Novo teste verificando população e limpeza de `TenantContext`, `SecurityContextHolder` e `MDC` (`requestId`, `tenantId`) em sucesso, token inválido/expirado, exceção na cadeia e reuso da mesma thread entre usuários e chamada anônima, além da ausência de vazamento de segredos/valores em logs de autenticação, erro e serviços (`DW-35`, `OBS-01..05`).
- `_bmad-output/implementation-artifacts/deferred-work.md` -- Atualizar `DW-35` e `DW-37` com data ISO, evidência de testes/revisão e encerramento conforme `rastreabilidade.md`.

## Tasks & Acceptance

**Execution:**
- [x] `backend-api/src/main/resources/application.yaml` -- Configurar padrão de log do Logback com chaves `requestId` e `tenantId` do `MDC` -- Exibir correlação por requisição e tenant no console sem infraestrutura externa (`OBS-03`)
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java` -- Preencher `MDC` (`requestId` e `tenantId` / `"anon"`), logar rejeições de token (`WARN`/`DEBUG`) sem expor o JWT/cookie e garantir limpeza de `MDC`, `TenantContext` e `SecurityContextHolder` em `finally` -- Assegurar correlação e isolamento por requisição (`OBS-01`, `OBS-02`, `DW-35`)
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java` & `backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java` -- Instrumentar logs `INFO`/`DEBUG` de login OAuth e logout com rota, resultado e `usuarioId` técnico, sem expor dados pessoais, JWT ou cookies -- Cobrir eventos de autenticação (`CAP-10` / `OBS-04`)
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java` -- Padronizar logs `WARN` (`400`/`403`/violações), `DEBUG` (`404`) e `ERROR` (`500` com stack trace) contendo status HTTP, exceção/regra, método e URI da requisição sem expor dados financeiros ou segredos -- Garantir rastreabilidade de erros e rejeições (`CAP-10`)
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/{ContaService,CategoriaService,DespesaFixaService,ReceitaFixaService,DespesaVariavelService,ReceitaVariavelService,ConsolidacaoService}.java` -- Instrumentar logs `INFO`/`WARN` nas mutações/validações com entidade, operação e IDs técnicos (`id`, `contaId`, `categoriaId`) e `DEBUG` na consolidação com contagem de itens e duração em ms, sem descrições ou valores monetários -- Rastrear operações críticas sem vazar dados privados (`CAP-10` / `OBS-04`)
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImplTest.java` & `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/ContextAndMdcLifecycleTest.java` -- Implementar testes automatizados para o contrato do resolver (`DW-37`), ciclo de vida/limpeza de `TenantContext`, `SecurityContextHolder` e `MDC` em sucesso, token inválido/expirado, exceção e reuso de thread (`DW-35`, `OBS-01..02`), e verificação via `OutputCaptureExtension`/appender de que logs de autenticação, erro e serviços contêm metadados obrigatórios e nunca expõem JWT, cookies, dados pessoais ou valores monetários (`OBS-04`, `OBS-05`)
- [x] `_bmad-output/implementation-artifacts/deferred-work.md` -- Atualizar `DW-35` e `DW-37` com data ISO, evidência de testes/revisão, pendências e estado `done` conforme `rastreabilidade.md` -- Cumprir protocolo obrigatório de conclusão

**Acceptance Criteria:**
- Given uma requisição processada pelo `JwtAuthenticationFilter` (autenticada, anônima, com token inválido/expirado ou lançando exceção na `FilterChain`), when inspecionados `TenantContext`, `SecurityContextHolder` e `MDC` durante e após a execução e em reuso subsequente da mesma thread por outro usuário ou chamada anônima, then os contextos e o `MDC` (`requestId`, `tenantId`) devem estar populados corretamente durante a requisição e integralmente limpos após o bloco `finally`, sem vazamento entre requisições (`DW-35`, `OBS-01`, `OBS-02`, `OBS-05`).
- Given o `CurrentTenantIdentifierResolverImpl`, when `TenantContext` contiver um ID válido, `null`, string vazia ou apenas espaços em branco, then `resolveCurrentTenantIdentifier()` deve retornar o ID do tenant quando preenchido e `"public"` nos demais casos, com `validateExistingCurrentSessions()` retornando `true` (`DW-37`).
- Given eventos de login OAuth, logout, rejeição de token, tratamento de exceções no `GlobalExceptionHandler` (`400`, `403`, `404`, `500`) e operações mutantes/consolidação nos serviços financeiros, when os logs forem emitidos, then devem conter os metadados técnicos exigidos (rota/URI, método, status, tipo/motivo, entidade, operação, IDs técnicos e duração/contagem na consolidação) e não devem conter em hipótese alguma tokens JWT, valores de cookies (`AUREUS_SESSION`, `XSRF-TOKEN`), valores monetários, descrições financeiras ou dados pessoais de perfil (`OBS-03`, `OBS-04`, `OBS-05`).

## Spec Change Log

- 2026-10-20: Revisão em 3 camadas (blind-hunter, edge-case-hunter, verification-gap) executada com patches aplicados: limpeza preventiva na entrada do filtro, null-safe em `WebUtils.getCookie`, sanitização de tipo em `GlobalExceptionHandler`, modernização `threadId()`, teste para subject vazio e teste de integração com `OutputCaptureExtension` em `SecurityIT`.

## Verification

**Commands:**
- `./mvnw test` -- expected: `BUILD SUCCESS` incluindo `CurrentTenantIdentifierResolverImplTest`, `ContextAndMdcLifecycleTest` e `GlobalExceptionHandlerTest`
- `./mvnw verify -Pintegration-test` -- expected: `BUILD SUCCESS` em toda a suíte de integração (`SecurityIT`, `MultiTenancyIT`, `RestContractAndIntegrityIT`, `BackendApplicationIT`)

## Suggested Review Order

**Isolamento de Contexto e Ciclo de Vida do MDC**

- População e remoção garantida de `requestId`, `tenantId`, `TenantContext` e `SecurityContextHolder` em bloco `finally`.
  [`JwtAuthenticationFilter.java:38`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java#L38)

- Contrato multi-tenant com fallback seguro `"public"` para contextos nulos ou em branco.
  [`CurrentTenantIdentifierResolverImpl.java:10`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java#L10)

**Formatação de Console e Observabilidade SLF4J**

- Padrão nativo de console com marcadores de correlação `[req:%X{requestId:-} tenant:%X{tenantId:-}]`.
  [`application.yaml:4`](../../backend-api/src/main/resources/application.yaml#L4)

- Logs técnicos padronizados para falhas 400, 403, 404 e 500 sem expor payloads sensíveis.
  [`GlobalExceptionHandler.java:40`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java#L40)

- Log de sucesso de autenticação OAuth com `usuarioId` técnico sem expor JWT ou perfil.
  [`OAuth2LoginSuccessHandler.java:71`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java#L71)

- Rastreabilidade técnica para consulta não autenticada em `/me` e encerramento via `/logout`.
  [`AuthController.java:36`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java#L36)

**Rastreabilidade nos Serviços de Negócio**

- Registro de auditoria técnica das mutações de conta sem detalhes descritivos.
  [`ContaService.java:38`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java#L38)

- Registro de auditoria técnica das mutações de categoria sem detalhes descritivos.
  [`CategoriaService.java:38`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java#L38)

- Mutações e rejeições de vínculo com IDs técnicos sem expor valores ou descrições.
  [`DespesaFixaService.java:44`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java#L44)

- Mutações e rejeições de vínculo com IDs técnicos sem expor valores ou descrições.
  [`ReceitaFixaService.java:44`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java#L44)

- Mutações e validações relacionais com IDs técnicos sem expor parcelas ou valores.
  [`DespesaVariavelService.java:54`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java#L54)

- Mutações e validações relacionais com IDs técnicos sem expor parcelas ou valores.
  [`ReceitaVariavelService.java:54`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java#L54)

- Métrica de itens processados e duração em ms do cálculo sem dados financeiros.
  [`ConsolidacaoService.java:168`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java#L168)

**Testes Automatizados e Rastreabilidade**

- Suíte de ciclo de vida dos contextos, reuso de thread e não vazamento em logs.
  [`ContextAndMdcLifecycleTest.java:153`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/ContextAndMdcLifecycleTest.java#L153)

- Verificação de contrato do resolver para valores preenchidos, nulos e strings vazias.
  [`CurrentTenantIdentifierResolverImplTest.java:16`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImplTest.java#L16)

- Teste de integração com `OutputCaptureExtension` confirmando pattern de log do Spring Boot.
  [`SecurityIT.java:416`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java#L416)

- Atualização formal de encerramento com evidências para `DW-35` e `DW-37`.
  [`deferred-work.md:96`](deferred-work.md#L96)
