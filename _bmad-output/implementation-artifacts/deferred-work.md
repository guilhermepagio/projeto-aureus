# Backlog consolidado — segurança e desempenho

Organizado em 2026-10-14 a partir das decisões de Guilherme no [relatório de triagem](../planning-artifacts/backlog-triage-2026-10-14.md).

## Como usar

- **Trabalho ativo:** somente entradas `DW-*` com `status: open` nas quatro seções de planejamento/investigação abaixo. `triage_id` mantém os IDs D do relatório; a numeração DW preserva o número D e tem lacunas intencionais. Novos DW devem continuar após o maior DW existente.
- **Manter — segurança:** correções/verificações selecionadas. `open` significa trabalho não executado. As aprovações para planejamento estão nas entradas; não autorizam implementação ou criação automática de épico.
- **Investigar:** coletar a evidência indicada antes de decidir correção. Ganhos de desempenho e vulnerabilidades hipotéticas não estão comprovados; estas entradas não autorizam otimização automática.
- **Histórico:** resolvidos, descartados, inaplicáveis e consolidados não são tarefas abertas. Descartado significa decisão de escopo, nunca correção entregue.
- O formato ativo usa campos em uma linha para permitir leitura por ferramentas BMad. Não há gates, stories ou specs novas. Fontes e evidências completas permanecem no relatório e na transcrição numerada do arquivo original ao final.

## Resumo

| Situação dos registros D | Quantidade |
|---|---:|
| Resolvido — histórico | 43 |
| Investigar — segurança | 2 |
| Descartado por critério | 91 |
| Manter — segurança | 8 |
| Encerrado — inaplicável | 7 |
| Consolidado | 11 |
| Investigar — desempenho | 5 |
| Planejar — condicional | 1 |
| Duplicado — histórico | 22 |
| **Total de origens D** | **190** |

Os 15 action items A ficam em `sprint-status.yaml`, com vínculos para estes representantes quando aplicável. Não somar registros consolidados como tarefas independentes.

## Planejamento aprovado — segurança

### DW-13: Restringir fallback de autorização e definir política pública do Actuator

origin: Registros iniciais sem seção; triage D013
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:40
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md
severity: critical
reason: Corrigir fallback permissivo e definir allowlist pública/Actuator. A autorização de rotas não API é aberta; não há prova de exposição de todos os endpoints Actuator em produção.
status: done
decision: 2026-10-14 Aprovado por Guilherme para planejamento no grupo 1 (acesso, cookies e logout); implementação concluída e verificada em 2026-10-19 na Story 7.1.
triage_id: D013
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:40](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:40); [backend-api/src/main/resources/application.yaml:39](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:39); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:269](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:269).
next_step: Nenhuma pendência; allowlist pública explícita, exposição restrita de /actuator/health (show-details: never) e fallback .anyRequest().denyAll() implementados e verificados.
data: 2026-10-19
historia: 7-1-hardening-de-autorizacao-cookies-de-sessao-e-matriz-residual
resultado: implementação verificada
evidencia: SecurityConfig.java, application.yaml e SecurityIT#allowlistPublicaEPoliticaActuatorDevemNegarRotasNaoAutorizadas (incluindo WebEndpointsSupplier e /error) executados via ./mvnw verify -Pintegration-test (BUILD SUCCESS, 205 testes unitários e 36 testes de integração verdes, diff auditado sobre baseline 6a83233024003de2d7823f927d92a9a1c071b432)
revisao: Revisão adversarial em 4 camadas (blind-hunter, edge-case-hunter, verification-gap, acceptance-auditor) em spec-7-1 concluída, 5 patches aplicados e aprovada contra SEG-01, SEG-02 e SEG-03 de seguranca.md
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-19

### DW-14: Verificar segurança do cookie de logout no backend

origin: Registros iniciais sem seção; triage D014
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md
severity: medium
reason: Manter somente testes de expiração e atributos do cookie de logout; testes de dados de perfil ficam descartados. Já há teste REST de status/Set-Cookie, faltam assertivas de segurança.
status: done
decision: 2026-10-14 Aprovado por Guilherme para planejamento no grupo 1 (acesso, cookies e logout); verificação concluída em 2026-10-19 na Story 7.1.
triage_id: D014
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D025/D022
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:42); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:209](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:209).
next_step: Nenhuma pendência; atributos de expiração e segurança do cookie AUREUS_SESSION no logout (anônimo e autenticado) e na limpeza por token expirado/inválido verificados em HTTP e HTTPS.
data: 2026-10-19
historia: 7-1-hardening-de-autorizacao-cookies-de-sessao-e-matriz-residual
resultado: verificação
evidencia: SecurityIT#endpointsDeAuthDevemFuncionarCorretamente, SecurityIT#requisicaoComTokenExpiradoDeveRetornar401ELimparCookie e SecurityIT#requisicaoComTokenInvalidoDeveRetornar401ELimparCookie executados via ./mvnw verify -Pintegration-test (BUILD SUCCESS, 9 testes em SecurityIT verdes, diff auditado sobre baseline 6a83233024003de2d7823f927d92a9a1c071b432)
revisao: Revisão adversarial em 4 camadas (blind-hunter, edge-case-hunter, verification-gap, acceptance-auditor) em spec-7-1 concluída, 5 patches aplicados e aprovada contra SEG-05 e SEG-06 de seguranca.md
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-19

### DW-26: Verificar logout e limpeza de estado/cache privado no frontend

origin: Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18); triage D026
location: frontend-web/src/components/Header/Header.tsx:13
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md
severity: medium
reason: Manter somente logout, limpeza de authStore/QueryClient e comportamento de falha de rede. Imagem/avatar/estado visual ficam descartados. Evita regressão na remoção local de dados privados; não comprova revogação do JWT no servidor.
status: done
decision: 2026-10-14 Aprovado por Guilherme para planejamento no grupo 1 (acesso, cookies e logout); verificação e testes automatizados concluídos em 2026-10-20 na Story 7.3, com patches de revisão validados em 2026-10-24.
triage_id: D026
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D045/D046
verification: [frontend-web/src/components/Header/Header.tsx:23](../../frontend-web/src/components/Header/Header.tsx#L23); [frontend-web/src/components/Header/Header.test.tsx:57](../../frontend-web/src/components/Header/Header.test.tsx#L57).
next_step: Nenhuma pendência; logout com sucesso (200) e sob falha de rede verificado com limpeza integral dos caches de queries e mutações, logout do authStore, redirecionamento para /login, toast informativo em erro de rede e proteção contra cliques concorrentes. Limpeza local com falha de rede não prova exclusão do cookie HttpOnly nem revogação do JWT no servidor (SEG-09).
data: 2026-10-24
historia: 7-3-tratamento-de-sessao-invalida-401-em-uso-e-limpeza-de-estado
resultado: verificação
evidencia: Header.tsx e Header.test.tsx (3 testes dedicados a logout com sucesso, falha de rede com toast informativo e prevenção de cliques concorrentes; caches de queries e mutações privadas preenchidos e verificados vazios após logout) executados via npm test --prefix frontend-web (20 arquivos e 205 testes aprovados) e npm run build --prefix frontend-web (aprovado), na árvore de trabalho sobre 159e2e8 em 2026-10-24.
revisao: Code review da Story 7.3 em quatro camadas, com 4 patches aplicados mediante autorização de Guilherme; cobertura do MutationCache adicionada e teste genérico de navegação removido. SEG-07 verificado; limpeza local sob falha de rede não comprova exclusão do cookie HttpOnly nem revogação do JWT no servidor (SEG-09).
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-24

### DW-35: Verificar população e limpeza do contexto de tenant e segurança

origin: Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18); triage D035
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md
severity: medium
reason: Testar set/clear de TenantContext e SecurityContextHolder em sucesso, falha e reuso de thread. Protege isolamento; implementação de finally já existe, não se presume vazamento atual.
status: done
decision: 2026-10-14 Grupo 2 — Aprovado para planejamento; testar limpeza dos contextos em sucesso, erro e reuso de thread; verificação concluída em 2026-10-20 na Story 7.2.
triage_id: D035
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:97](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:97); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/ContextAndMdcLifecycleTest.java:112](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/ContextAndMdcLifecycleTest.java:112); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:416](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:416).
next_step: Nenhuma pendência; população e limpeza de TenantContext, SecurityContextHolder e MDC (requestId, tenantId) em sucesso, token inválido/expirado, subject vazio, exceção na cadeia e reuso de thread verificadas.
data: 2026-10-20
historia: 7-2-isolamento-de-contexto-correlacao-mdc-e-logs-estruturados
resultado: implementação verificada
evidencia: JwtAuthenticationFilter.java, ContextAndMdcLifecycleTest (9 testes cobrindo população/limpeza de TenantContext, SecurityContextHolder e MDC em sucesso, token inválido/expirado, subject vazio, exceção e reuso da mesma thread, além de logs sem vazamento sensível) e SecurityIT (10 testes incluindo verificação com OutputCaptureExtension da emissão real do pattern de console com [req: e tenant:]) executados via ./mvnw test e ./mvnw verify -Pintegration-test (BUILD SUCCESS, 219 testes unitários e 37 testes de integração verdes, sobre baseline 00434e9978d132de880cb0874eaf9e5e77a3bf64)
revisao: Revisão adversarial em 3 camadas (blind-hunter, edge-case-hunter, verification-gap) em spec-7-2 concluída, patches aplicados e aprovada contra SEG-10 de seguranca.md e OBS-01..05 de observabilidade.md
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-20

### DW-36: Verificar atributos de segurança e TTL do cookie no sucesso OAuth

origin: Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18); triage D036
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md
severity: medium
reason: Testar emissão OAuth de cookie HttpOnly, SameSite, Secure condicionado ao transporte e TTL. Flags já existem; ganho é prevenir regressão de segurança, não corrigir vulnerabilidade comprovada.
status: done
decision: 2026-10-14 Aprovado por Guilherme para planejamento no grupo 1 (acesso, cookies e logout); verificação concluída em 2026-10-19 na Story 7.1.
triage_id: D036
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:345](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:345).
next_step: Nenhuma pendência; atributos HttpOnly, Path=/, SameSite=Lax, Max-Age=86400 coerente com jwt.expiration e Secure condicionado ao transporte verificados em HTTP e HTTPS no OAuth2LoginSuccessHandler.
data: 2026-10-19
historia: 7-1-hardening-de-autorizacao-cookies-de-sessao-e-matriz-residual
resultado: verificação
evidencia: SecurityIT#oauth2LoginSuccessHandlerDeveEmitirCookieComAtributosDeSegurancaEmHttpEHttps executado via ./mvnw verify -Pintegration-test (BUILD SUCCESS, 9 testes em SecurityIT verdes, diff auditado sobre baseline 6a83233024003de2d7823f927d92a9a1c071b432)
revisao: Revisão adversarial em 4 camadas (blind-hunter, edge-case-hunter, verification-gap, acceptance-auditor) em spec-7-1 concluída, 5 patches aplicados e aprovada contra SEG-04 de seguranca.md
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-19

### DW-37: Verificar contrato do resolver de tenant

origin: Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18); triage D037
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md
severity: medium
reason: Testar contrato do resolver com tenant preenchido/nulo/vazio; vincular à investigação D160. Não alterar fallback automaticamente nem duplicar integração cross-tenant existente.
status: done
decision: 2026-10-14 Grupo 2 — Aprovado para planejamento; testar o resolver junto à investigação DW-160; verificação do contrato vigente concluída em 2026-10-20 na Story 7.2.
triage_id: D037
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —; origens duplicadas: D044
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImplTest.java:11](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImplTest.java:11); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
next_step: Nenhuma pendência no contrato vigente do resolver (SEG-11); investigação sobre acesso sem contexto e bootstrap segue rastreada em DW-160 (Story 7.5).
data: 2026-10-20
historia: 7-2-isolamento-de-contexto-correlacao-mdc-e-logs-estruturados
resultado: verificação
evidencia: CurrentTenantIdentifierResolverImplTest (5 testes unitários cobrindo tenant preenchido, null, string vazia, espaços em branco e validateExistingCurrentSessions() == true) executado via ./mvnw test e ./mvnw verify -Pintegration-test (BUILD SUCCESS, 219 testes unitários e 37 testes de integração verdes, sobre baseline 00434e9978d132de880cb0874eaf9e5e77a3bf64)
revisao: Revisão adversarial em 3 camadas (blind-hunter, edge-case-hunter, verification-gap) em spec-7-2 concluída contra SEG-11 de seguranca.md preservando integralmente o contrato vigente do resolver
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-20

### DW-60: Tratar 401 durante uso e limpar estado/cache privado

origin: Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18); triage D060
location: frontend-web/src/services/apiClient.ts:176
severity: medium
reason: Limpar estado/cache privado e tratar sessão expirada após 401 em chamadas durante uso. Reduz permanência de dados privados na UI; backend já bloqueia API, portanto não é correção de bypass de autorização.
status: done
decision: 2026-10-14 Aprovado por Guilherme para planejamento no grupo 3; implementação e testes concluídos em 2026-10-20 na Story 7.3, com patches de revisão validados em 2026-10-24.
triage_id: D060
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [frontend-web/src/services/apiClient.ts:195](../../frontend-web/src/services/apiClient.ts#L195); [frontend-web/src/App.tsx:48](../../frontend-web/src/App.tsx#L48); [frontend-web/src/services/apiClient.test.ts:454](../../frontend-web/src/services/apiClient.test.ts#L454); [frontend-web/src/App.test.tsx:114](../../frontend-web/src/App.test.tsx#L114).
next_step: Nenhuma pendência; 401 local invalida a sessão antes da leitura do corpo, limpa authStore e caches de queries/mutações e navega para /login. Comparação de origens resolvidas evita logout por destinos externos; timeout após headers 401 preserva ApiError 401; contratos 403/404/400/500 permanecem preservados. Limpeza local não prova revogação do JWT no servidor (SEG-09).
data: 2026-10-24
historia: 7-3-tratamento-de-sessao-invalida-401-em-uso-e-limpeza-de-estado
resultado: implementação verificada
evidencia: apiClient.test.ts (41 testes incluindo URLs locais normalizadas, 6 formatos externos, invalidação antes do corpo, preservação de 401 sob timeout, demais status e bypass), App.test.tsx (4 testes incluindo limpeza dos caches de queries/mutações após 401 e inicialização anônima) e Header.test.tsx (3 testes de logout) executados via npm test --prefix frontend-web (20 arquivos e 205 testes aprovados) e npm run build --prefix frontend-web (aprovado), na árvore de trabalho sobre 159e2e8 em 2026-10-24.
revisao: Code review da Story 7.3 em quatro camadas, com 4 patches aplicados mediante autorização de Guilherme; origem, momento da invalidação e cobertura do MutationCache corrigidos e validados contra SEG-08. Limpeza no cliente não comprova exclusão do cookie HttpOnly nem revogação antecipada do JWT no servidor (SEG-09).
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-24

### DW-176: Definir e completar matriz específica de testes CSRF e vínculos cruzados

origin: Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05); triage D176
location: backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:207
severity: medium
reason: Manter só matriz de segurança CSRF PUT/DELETE sem token e vínculos cruzados não exercitados. Expansão exaustiva de DTOs/validação genérica fica descartada.
status: done
decision: 2026-10-14 Grupo 2 — Aprovado para planejamento somente o recorte CSRF PUT/DELETE sem token e vínculos cruzados faltantes; excluir expansão genérica de DTOs. Verificação concluída em 2026-10-19 na Story 7.1.
triage_id: D176
triage_classification: Necessita esclarecimento ou validação adicional
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D122/D164
verification: [backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:167](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:167); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:207](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:207).
next_step: Nenhuma pendência; matriz residual de PUT/DELETE sem CSRF (403 sem mutação), vínculos cruzados POST/PUT (403 sem mutação) e recursos alheios GET de listagem (ocultação por @TenantId) e PUT/DELETE /{id} (404 sem mutação) completa e verificada.
data: 2026-10-19
historia: 7-1-hardening-de-autorizacao-cookies-de-sessao-e-matriz-residual
resultado: verificação
evidencia: SecurityIT#requisicoesPutEDeleteSemCsrfDevemRetornar403EManterRegistroIntacto e MultiTenancyIT (22 testes cobrindo POST/PUT 403 e GET listagem / PUT/DELETE 404 nas entidades multi-tenant) executados via ./mvnw verify -Pintegration-test (BUILD SUCCESS, 36 testes de integração verdes, diff auditado sobre baseline 6a83233024003de2d7823f927d92a9a1c071b432)
revisao: Revisão adversarial em 4 camadas (blind-hunter, edge-case-hunter, verification-gap, acceptance-auditor) em spec-7-1 concluída, 5 patches aplicados e aprovada contra SEG-14, SEG-15 e SEG-16 de seguranca.md
pendencias: nenhuma
estado_do_registro: done
data_da_resolucao: 2026-10-19

## Planejamento condicional — índices

### DW-161: Validar e eliminar redundância de índice em google_subject_id

origin: Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16); triage D161
location: backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4
source_spec: _bmad-output/implementation-artifacts/archive/epic-5/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
source_spec_original: _bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
severity: medium
reason: Conferir catálogo e remover índice secundário redundante de google_subject_id preservando UNIQUE. Reduz espaço/manutenção se ambos existirem como no DDL; nenhuma melhoria de leitura garantida.
status: open
decision: 2026-10-14 Aprovado por Guilherme para planejamento no grupo 4, vinculado a A13; remoção condicionada à conferência do banco. Sem implementação.
triage_id: D161
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: A13/D155
verification: [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:4); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74).
next_step: Planejar conferência do catálogo e remoção somente do índice secundário redundante, preservando UNIQUE; nenhuma migração executada nesta etapa.

## Investigações aprovadas — segurança

### DW-2: Definir necessidade de revogação e renovação da sessão

origin: Registros iniciais sem seção; triage D002
location: _bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15
severity: medium
reason: Decidir necessidade de revogação/renovação de sessão. Investigar risco/contrato; não exigir entidade refresh token como solução automática.
status: open
decision: 2026-10-14 Investigação aprovada por Guilherme no grupo 3, restrita à revogação de sessão; renovação automática não aprovada. Sem implementação.
triage_id: D002
triage_classification: Necessita esclarecimento ou validação adicional
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D007/D008
verification: [_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15](/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/planning-artifacts/decisions/2026-08-13-autenticacao-v1.md:15); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:61); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44).
next_step: Avaliar necessidade e custo de invalidar JWT antes da expiração; não introduzir refresh token ou tabela de sessões sem decisão posterior.

### DW-160: Validar isolamento quando o contexto de tenant está ausente

origin: Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16); triage D160
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10
source_spec: _bmad-output/implementation-artifacts/archive/epic-5/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
source_spec_original: _bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
severity: medium
reason: Investigar acesso/persistência sem contexto e requisitos de bootstrap antes de trocar fallback public. Risco de isolamento ainda não demonstrado.
status: open
decision: 2026-10-14 Grupo 2 — Investigação aprovada, incluindo DW-38; confirmar isolamento sem contexto e bootstrap antes de decidir alteração.
triage_id: D160
triage_classification: Necessita esclarecimento ou validação adicional
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D037/D038
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:44); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9).
next_step: Investigação aprovada, incluindo DW-38; confirmar isolamento sem contexto e bootstrap antes de decidir alteração.

## Investigações aprovadas — desempenho

### DW-117: Medir necessidade de paginação nas listagens de cadastros e movimentações

origin: Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md; triage D117
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43
source_spec: /home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md
severity: medium
reason: Consolidar paginação de Contas, Categorias e movimentações. Listas sem limite confirmadas; necessidade depende do volume/latência. Seletores não podem perder opções ao paginar.
status: open
decision: 2026-10-14 Investigação aprovada por Guilherme no grupo 5: medir volume, tamanho de resposta e latência para decidir paginação, preservando acesso completo dos seletores. Implementação condicionada aos resultados.
triage_id: D117
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D075/D088/D105
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:23); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:23).
next_step: Investigação aprovada por Guilherme no grupo 5: medir volume, tamanho de resposta e latência para decidir paginação, preservando acesso completo dos seletores.

### DW-139: Medir e avaliar custo da consolidação sem perder saldo histórico

origin: Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md); triage D139
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74
severity: medium
reason: Medir leitura integral e agregação da consolidação e escolher SQL/corte por período se o custo justificar. Preservar cálculo de saldo histórico, que usa período anterior à grade.
status: open
decision: 2026-10-14 Investigação aprovada por Guilherme no grupo 4, vinculada a A10. Sem implementação.
triage_id: D139
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D162/A10; origens duplicadas: D142, D162, A10
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:74); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:262).
next_step: Medir tempo, memória e volume de consultas; propor otimização apenas com custo relevante e equivalência dos saldos mensais/históricos.

### DW-147: Verificar se conversão de DTOs provoca consultas N+1

origin: Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15); triage D147
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43
severity: medium
reason: Contar consultas na conversão DTO antes de adotar JOIN FETCH. LAZY e acesso ao ID não comprovam N+1.
status: open
decision: 2026-10-14 Investigação aprovada por Guilherme no grupo 5: contar SQL real nas listagens; DTOs de movimentação acessam descrição/observações de conta/categoria LAZY, não apenas IDs. N+1 ainda não medido. Implementação condicionada aos resultados.
triage_id: D147
triage_classification: Necessita esclarecimento ou validação adicional
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:43).
next_step: Investigação aprovada por Guilherme no grupo 5: contar SQL real nas listagens; DTOs de movimentação acessam descrição/observações de conta/categoria LAZY, não apenas IDs. N+1 ainda não medido.

### DW-153: Medir custo das consultas de validação de propriedade dos relacionamentos

origin: Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16); triage D153
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52
source_spec: _bmad-output/implementation-artifacts/archive/epic-5/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
source_spec_original: _bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
severity: medium
reason: Medir custo das duas consultas de validação de relacionamento; reduzir roundtrips preservando propriedade do tenant e respostas 403/404.
status: open
decision: 2026-10-14 Investigação aprovada por Guilherme no grupo 5 com prioridade menor: medir custo da consulta de proprietário seguida da entidade, preservando checagem de tenant e contratos 403/404. Implementação condicionada aos resultados.
triage_id: D153
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:52).
next_step: Investigação aprovada por Guilherme no grupo 5 com prioridade menor: medir custo da consulta de proprietário seguida da entidade, preservando checagem de tenant e contratos 403/404.

### DW-155: Avaliar índices de FKs e de usuário em contas/categorias

origin: Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16); triage D155
location: backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74
source_spec: _bmad-output/implementation-artifacts/archive/epic-5/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
source_spec_original: _bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md
severity: medium
reason: Validar planos/catálogo e volume para índices de FKs e usuario_id de contas/categorias. Benefício de busca/delete plausível, mas índices também custam escrita e espaço.
status: open
decision: 2026-10-14 Investigação aprovada por Guilherme no grupo 4, vinculada a A13. Atualizado em 2026-10-19 por Guilherme: criação de índices para usuario_id em contas e categorias passa a ser implementação obrigatória na migração incremental Flyway V2 (nas 4 tabelas de movimentações usuario_id já lidera índices compostos na V1); avaliação das FKs (conta_id, categoria_id) e dos índices de data sem usuario_id permanece investigativa. Sem execução nesta etapa.
triage_id: D155
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: A13/D161
verification: [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:74); [backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/db/migration/V1__create_performance_indexes.sql:24).
next_step: Planejar na migração Flyway V2 os índices obrigatórios idx_contas_usuario_id e idx_categorias_usuario_id; conferir catálogo, consultas e planos antes de decidir índices de FKs e limpeza de índices sem usuario_id.

## Registros consolidados e descartados — histórico

### Registro consolidado DW-38: Validar configuração e ciclo de vida do resolver de tenant

origin: Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18); triage D038
location: backend-api/src/main/resources/application.yaml:8
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md
severity: medium
reason: Consolidar validação da configuração do resolver em D160; nome de classe e ausência de bean não comprovam falha por si.
status: consolidated
decision: 2026-10-14 Grupo 2 — Consolidado por decisão de Guilherme em DW-160; sem tarefa independente e sem implementação.
triage_id: D038
triage_classification: Necessita esclarecimento ou validação adicional
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: D037
verification: [backend-api/src/main/resources/application.yaml:8](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/resources/application.yaml:8); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:10); [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java:206).
next_step: Consultar DW-160; registro preservado somente para rastreabilidade.

### Registro descartado DW-63: Verificar se há fluxo assíncrono que exige contexto de tenant

origin: Deferred from: code review of spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md (2026-08-18); triage D063
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9
severity: medium
reason: Identificar consumidor assíncrono real de dados por tenant. Sem esse fluxo, propagação de contexto não é requisito necessário.
status: discarded
decision: 2026-10-14 Grupo 2 — Descartado por decisão de Guilherme; nenhum consumidor assíncrono que exija propagação foi identificado no backend atual. Não implementado.
triage_id: D063
triage_classification: Necessita esclarecimento ou validação adicional
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java:9); [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java:57).
next_step: Nenhuma ação; conservar histórico do descarte.

### Registro descartado DW-27: Medir consultas e possíveis escritas no login

origin: Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18); triage D027
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md
severity: medium
reason: Medir SQL real no login antes de otimizar. save() é chamado sempre, mas não prova UPDATE em toda autenticação: detecção de alterações pode evitar escrita. Não prometer ganho sem tracing.
status: discarded
decision: 2026-10-14 Descartado por Guilherme no grupo 6: save() no login não prova escrita SQL desnecessária; nenhum custo relevante demonstrado. Perfil continua sendo atualizado no login. Não implementado.
triage_id: D027
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java:41).
next_step: Nenhuma ação neste planejamento; reconsiderar apenas se futura medição demonstrar custo relevante.

### Registro descartado DW-28: Medir custo e frequência da consulta de perfil /me

origin: Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18); triage D028
location: backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29
source_spec: _bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md
severity: medium
reason: Medir frequência/latência de /me; há leitura por chamada. Decidir cache só se custo relevante e mantendo frescor/isolamento de perfil.
status: discarded
decision: 2026-10-14 Descartado por Guilherme no grupo 6: chamada de /me identificada na inicialização, sem polling; nenhum gargalo que justifique cache de perfil neste planejamento. Não implementado.
triage_id: D028
triage_classification: Necessita esclarecimento ou validação adicional
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29](/home/guilhermepagio/developer/workspace/projeto-aureus/backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java:29).
next_step: Nenhuma ação neste planejamento; reconsiderar apenas se futura medição demonstrar custo relevante.

### Registro descartado DW-92: Avaliar cancelamento de consultas de categorias obsoletas

origin: Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md; triage D092
location: frontend-web/src/hooks/useCategorias.ts:14
severity: medium
reason: Encaminhar AbortSignal da query pode interromper requisição obsoleta no cliente. Medir relevância; não garante cancelamento de trabalho já iniciado no servidor.
status: discarded
decision: 2026-10-14 Descartado por Guilherme no grupo 6: cancelamento pode economizar transferência, mas não foi identificado impacto relevante; cancelar no cliente não garante cancelar trabalho no servidor. Não implementado.
triage_id: D092
triage_classification: Pendente confirmado
triage_report: _bmad-output/planning-artifacts/backlog-triage-2026-10-14.md
related_records: —
verification: [frontend-web/src/hooks/useCategorias.ts:14](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/hooks/useCategorias.ts:14); [frontend-web/src/services/apiClient.ts:140](/home/guilhermepagio/developer/workspace/projeto-aureus/frontend-web/src/services/apiClient.ts:140).
next_step: Nenhuma ação neste planejamento; reconsiderar apenas se futura medição demonstrar custo relevante.

## Histórico das origens D

Cada origem aparece uma vez abaixo. O texto original completo, inclusive referências e alegações antigas, está preservado na transcrição numerada. Classificações e decisões detalhadas constam no relatório. Consolidação não significa execução.

| ID de triagem | Descrição original | Classificação técnica | Decisão | Representante / encaminhamento |
|---|---|---|---|---|
| D001 | Regenerate aureus-erd.png | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D002 | Add session/refresh token entity to data and domain models | Necessita esclarecimento ou validação adicional | **Investigar — segurança** | DW-2 — trabalho ativo; D007/D008 |
| D003 | Add audit attributes to domain classes (Conta, Categoria, Despesa, Receita) | Pendente confirmado | **Descartado por critério** | D119/D048 |
| D004 | Sync Despesa attributes between class diagram and DBML | Pendente confirmado | **Descartado por critério** | — |
| D005 | Sync Receita attributes between class diagram and DBML | Pendente confirmado | **Descartado por critério** | — |
| D006 | Resolve structural discrepancy for TipoMovimento | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D007 | Story 1.2 - Autenticação via Google (OAuth 2.0) e Tratamento de Erros | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D008 | Story 1.3 - Proteção de Sessão e Isolamento de Dados | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D009 | Story 1.4 - Encerrar Sessão (Logout) e Limpeza de Estado | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: D057 |
| D010 | Add Navigation icons | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D011 | Add semantic states colors to CSS tokens | Pendente confirmado | **Descartado por critério** | — |
| D012 | Add automated tests for client-side routing | Pendente confirmado | **Descartado por critério** | D068/D098; origens duplicadas: D068 |
| D013 | Fix permissive authorization fallback and protect Actuator endpoints | Pendente confirmado | **Manter — segurança** | DW-13 — trabalho ativo; — |
| D014 | Implement backend unit tests for AuthController | Pendente confirmado | **Manter — segurança** | DW-14 — trabalho ativo; D025/D022 |
| D015 | Implement frontend unit tests for Navigation component | Obsoleto ou inaplicável | **Encerrado — inaplicável** | D026/D012 |
| D016 | Configure global defaults for React QueryClient | Pendente confirmado | **Descartado por critério** | — |
| D017 | Configure secure attribute for session cookie based on environment | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D018 | Interface de carregamento (Loading) não estlizada [frontend/src/App.tsx] | Pendente confirmado | **Descartado por critério** | — |
| D019 | Redirecionamento de login não preserva o estado de rota prévia (`location.state`) [frontend/src/App.tsx:47] | Pendente confirmado | **Descartado por critério** | — |
| D020 | Rotas hardcoded ao invés de usar constantes centralizadas [frontend/src/App.tsx] | Pendente confirmado | **Descartado por critério** | — |
| D021 | Ausência de Error Boundary genérico para falhas do React [frontend/src/App.tsx] | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: D056 |
| D022 | Ausência de testes end-to-end e componentes para o Auth Fetch e Logout | Pendente confirmado | **Consolidado** | D014/D026/A11 |
| D023 | Ausência de verificação contra bypass de rota protegida | Pendente confirmado | **Descartado por critério** | D012 |
| D024 | Missing verification for Google profile picture extraction | Pendente confirmado | **Descartado por critério** | — |
| D025 | Missing verification for profile picture inclusion in /me response | Pendente confirmado | **Descartado por critério** | D014/D026 |
| D026 | Missing verification for Header logout and profile UI | Pendente confirmado | **Manter — segurança** | DW-26 — trabalho ativo; D045/D046 |
| D027 | Unnecessary Database Writes on Login | Pendente confirmado | **Descartado por critério** | DW-27 — descartado; — |
| D028 | Inefficient Profile Fetching | Necessita esclarecimento ou validação adicional | **Descartado por critério** | DW-28 — descartado; — |
| D029 | Direct Repository Access in AuthController | Pendente confirmado | **Descartado por critério** | — |
| D030 | Missing Strongly Typed DTO in AuthController | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D031 | Hardcoded frontend redirect URI in OAuth2LoginSuccessHandler. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D032 | Overly broad exception catching in JwtAuthenticationFilter. | Pendente confirmado | **Descartado por critério** | — |
| D033 | Empty Response Body on 401 Unauthorized in JwtAuthenticationFilter. | Pendente confirmado | **Descartado por critério** | D157 |
| D034 | Potential Token and Cookie Expiration Mismatch. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D035 | Missing verification for Tenant context population. | Pendente confirmado | **Manter — segurança** | DW-35 — trabalho ativo; — |
| D036 | Missing verification for Session cookie SameSite security attribute. | Pendente confirmado | **Manter — segurança** | DW-36 — trabalho ativo; — |
| D037 | Missing verification for Hibernate tenant resolution. | Pendente confirmado | **Manter — segurança** | DW-37 — trabalho ativo; —; origens duplicadas: D044 |
| D038 | Loss of Spring Context in Tenant Resolver | Necessita esclarecimento ou validação adicional | **Consolidado** | DW-160 — representante; DW-38 consolidado; D037 |
| D039 | Security Vulnerability via URL Token Transmission | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: D047 |
| D040 | Missing JWT Validation Error Handling | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: D052 |
| D041 | Unrecoverable Profile Image Error State | Pendente confirmado | **Descartado por critério** | — |
| D042 | Hardcoded Avatar Fallback | Pendente confirmado | **Descartado por critério** | —; origens duplicadas: D071 |
| D043 | Non-Semantic DOM Structure | Obsoleto ou inaplicável | **Encerrado — inaplicável** | — |
| D044 | Tenant identifier resolver configuration lacks verification | Duplicado | **Duplicado — histórico** | D037 |
| D045 | Profile image referrer policy is unverified | Pendente confirmado | **Descartado por critério** | D026 |
| D046 | Application navigation layout composition is unverified | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D047 | OAuth2LoginSuccessHandler Cookie import removed | Duplicado | **Duplicado — histórico** | D039 |
| D048 | OAuth2LoginSuccessHandler missing audit logging | Pendente confirmado | **Descartado por critério** | D003/D119 |
| D049 | OAuth2LoginSuccessHandler missing error handling | Pendente confirmado | **Descartado por critério** | — |
| D050 | UsuarioRepository missing @Repository annotation | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D051 | UsuarioRepository missing google_subject_id index | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D052 | JwtUtil JwtException import removed | Duplicado | **Duplicado — histórico** | D040 |
| D053 | Tenant isolation missing tests | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D054 | application.yaml OAuth2 hardcoded secrets risk | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D055 | Header CSS hardcoded z-index | Pendente confirmado | **Descartado por critério** | — |
| D056 | Entire React application unmounts on route rendering error [frontend/src/App.tsx:68] | Duplicado | **Duplicado — histórico** | D021 |
| D057 | Missing Backend Logout Endpoint (belongs to Story 1.4) | Duplicado | **Duplicado — histórico** | D009 |
| D058 | Incomplete CSRF Setup on Frontend | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D059 | Missing automated tests for auth flows | Pendente confirmado | **Consolidado** | D022/D024/D026 |
| D060 | Missing frontend interception of 401 errors during API access | Pendente confirmado | **Manter — segurança** | DW-60 — trabalho ativo; — |
| D061 | Concurrent OAuth logins for same new user could cause DataIntegrityViolationException | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D062 | Missing Automated Tests | Necessita esclarecimento ou validação adicional | **Consolidado** | D035/D036/D037/D053 |
| D063 | Lack of Asynchronous Thread Context Propagation | Necessita esclarecimento ou validação adicional | **Descartado por critério** | DW-63 — descartado; — |
| D064 | Vazamento de contexto no TenantContext: Usando ThreadLocal padrão. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D065 | Omissão de acessibilidade no dropdown do Header (Esc): Falta listener para a tecla Escape. | Pendente confirmado | **Descartado por critério** | — |
| D066 | Preenchimento extra no mobile para rodapé: O .main-content tem padding inferior de 80px herdado da antiga navegação. | Obsoleto ou inaplicável | **Encerrado — inaplicável** | — |
| D067 | Ausência de testes de integração e endpoints backend: verification-gap acusou falta de testes para controllers e filtros criados. | Necessita esclarecimento ou validação adicional | **Consolidado** | D014/D024/D035 |
| D068 | Adicionar testes de navegação para certificar que rotas não-raiz renderizam o conteúdo esperado. | Duplicado | **Duplicado — histórico** | D012 |
| D069 | Adicionar teste automatizado E2E ou de artefatos para verificar se os estilos do Tailwind são processados e aplicados com sucesso na build. | Pendente confirmado | **Descartado por critério** | — |
| D070 | Corrigir a11y: adicionar aria-live e role="status" ao estado de carregamento e aria-controls ao botão de perfil. | Pendente confirmado | **Descartado por critério** | — |
| D071 | Utilizar a inicial do usuário logado no avatar padrão em vez de 'U' fixo. | Duplicado | **Duplicado — histórico** | D042 |
| D072 | Adicionar testes de unidade e integração (Backend e Frontend) para Contas | Pendente confirmado | **Descartado por critério** | D180/D083 |
| D073 | Adicionar métodos equals() e hashCode() na entidade Conta | Necessita esclarecimento ou validação adicional | **Descartado por critério** | —; origens duplicadas: D081 |
| D074 | Implementar Focus Trap acessível e completo no Modal | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: D078 |
| D075 | Adicionar suporte à paginação no backend e listagem de Contas | Pendente confirmado | **Consolidado** | —; origens duplicadas: D080 |
| D076 | Adicionar tratamento e exibição de erro da API com parsing estruturado no frontend | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D077 | Endpoint criar Conta retorna 200 OK em vez de 201 Created. | Pendente confirmado | **Descartado por critério** | — |
| D078 | Modal genérico não possui Focus Trap acessível. | Duplicado | **Duplicado — histórico** | D074 |
| D079 | Tratamento de erros de validação server-side não mapeados no formulário. | Pendente confirmado | **Descartado por critério** | — |
| D080 | Endpoint de listagem de contas não é paginado. | Duplicado | **Duplicado — histórico** | D075 |
| D081 | Entidade Conta sem implementação customizada de equals() e hashCode(). | Duplicado | **Duplicado — histórico** | D073 |
| D082 | Modal não se adapta dinamicamente para Bottom Sheet em mobile. | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D083 | Ausência de testes verificando isolamento cross-tenant e invalidação de cache. | Pendente confirmado | **Descartado por critério** | D072/D126 |
| D084 | Ausência de testes para contrato de violação de FK na deleção. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D085 | Endpoint criar Categoria retorna 200 OK em vez de 201 Created. | Pendente confirmado | **Descartado por critério** | D077 |
| D086 | Entidade Categoria sem métodos equals() e hashCode(). | Necessita esclarecimento ou validação adicional | **Descartado por critério** | D073 |
| D087 | O endpoint de exclusão assume 400 apenas como FK violation. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D088 | Listagem de Categorias não possui paginação. | Pendente confirmado | **Consolidado** | D075/D105 |
| D089 | Falta de tratamento de timeout para requisições frontend. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D090 | Testes automatizados ausentes para Categoria. | Pendente confirmado | **Descartado por critério** | D083 |
| D091 | Missing Location header in Categoria creation. | Pendente confirmado | **Descartado por critério** | — |
| D092 | useCategorias.ts lacks AbortSignal. | Pendente confirmado | **Descartado por critério** | DW-92 — descartado; — |
| D093 | Optimistic locking (missing @Version). | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D094 | Improve UX by checking both accounts and categories simultaneously to avoid a "waterfall" of empty states. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D095 | Add data-testid attributes to loading, error, and empty states. | Obsoleto ou inaplicável | **Encerrado — inaplicável** | —; origens duplicadas: D101 |
| D096 | Add a Retry button to the error state. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D097 | Improve error state UI to match the polished, card-based layouts of the empty states. | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D098 | Add automated tests to verify dependency guards block access to transaction routes. | Pendente confirmado | **Descartado por critério** | — |
| D099 | Missing `role="status"` on loading state. | Pendente confirmado | **Descartado por critério** | D188 |
| D100 | Missing `role="alert"` on error state. | Pendente confirmado | **Descartado por critério** | D188 |
| D101 | Missing `data-testid` attributes on conditional UI states. | Duplicado | **Duplicado — histórico** | D095 |
| D102 | Repetitive `<ProtectedRoute><RequiresDependencies>` wrappers in App.tsx. | Pendente confirmado | **Descartado por critério** | — |
| D103 | Scope creep / missing `aria-hidden="true"`: Unrequested additions of `lucide-react` icons in Header.tsx. | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D104 | Missing Relationship Ownership Validation (verify if related conta/categoria belong to user). | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: D109 |
| D105 | Missing Pagination for /api/despesas-fixas and /api/receitas-fixas. | Pendente confirmado | **Consolidado** | D075/D117; origens duplicadas: D111 |
| D106 | Clarify if fixed items need an end date (dataFim) or active flag to prevent history mutation on deletion. | Necessita esclarecimento ou validação adicional | **Descartado por critério** | —; origens duplicadas: D110 |
| D107 | Missing backend integration tests for new endpoints. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D108 | Missing frontend component tests for DespesasFixasPage and ReceitasFixasPage. | Pendente confirmado | **Descartado por critério** | — |
| D109 | Falta de validação de propriedade (Tenant) nos relacionamentos informados (conta/categoria) | Duplicado | **Duplicado — histórico** | D104 |
| D110 | Mutação de histórico financeiro em exclusões (falta de dataFim/soft-delete) | Duplicado | **Duplicado — histórico** | D106 |
| D111 | Falta de paginação nos endpoints `GET` de listagem | Duplicado | **Duplicado — histórico** | D105 |
| D112 | Falta de cobertura de testes automatizados (integração e UI) | Necessita esclarecimento ou validação adicional | **Descartado por critério** | D107/D108 |
| D113 | Controllers return JPA Entities directly instead of using DTOs, risking Mass Assignment | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D114 | Duplicated date logic (preencherDataFim) in controllers instead of centralizing in service or lifecycle hook | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D115 | Missing DB indexes for dataInicio and dataFim which will be heavily queried in Epic 4 | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D116 | Missing class-level validation to ensure dataFim >= dataInicio | Obsoleto ou inaplicável | **Encerrado — inaplicável** | — |
| D117 | Unbounded findAll queries in backend list endpoints | Pendente confirmado | **Investigar — desempenho** | DW-117 — trabalho ativo; D075/D088/D105 |
| D118 | DRY violation in frontend form/list pages for variable expenses and revenues | Necessita esclarecimento ou validação adicional | **Descartado por critério** | A04/D178 |
| D119 | Missing Auditing fields (createdAt, updatedAt) across entities | Pendente confirmado | **Descartado por critério** | D003/D048 |
| D120 | Lack of global @ControllerAdvice for standard constraint violations | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D121 | Concurrent deletion causes unhandled 500 EmptyResultDataAccessException | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D122 | Missing automated verification tests (dataFim, relational errors, API parsing) | Necessita esclarecimento ou validação adicional | **Descartado por critério** | D176 |
| D123 | Missing DTOs, Service Layer, and Global Exception Handler | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D124 | Fetcher swallows non-JSON 500 errors | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D125 | Fixed transactions lack start/end dates in the domain model, preventing month-based filtering. | Pendente confirmado | **Descartado por critério** | D106/D108/D156 |
| D126 | Missing test coverage for React Query mutation cache invalidations and global month filter logic. | Pendente confirmado | **Descartado por critério** | D083/D090 |
| D127 | `epic-3-context.md` was translated to English, losing technical specificity and causing context fragmentation with Portuguese PRDs. | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D128 | monthStore currentMonth is initialized at module load and can become stale. | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D129 | No UI exists to change the selected month. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D130 | Duplicated toggle switch and SVG icon markup. | Pendente confirmado | **Descartado por critério** | — |
| D131 | useContas and useCategorias do not invalidate ['consolidacao']. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D132 | Adicionar testes de unidade para ConsolidacaoPage, ConsolidacaoGrid e ConsolidacaoToolbar garantindo que as 24 colunas são renderizadas e o seletor funciona. | Pendente confirmado | **Descartado por critério** | — |
| D133 | O hook useConsolidacao utiliza fetch diretamente, possivelmente contornando clientes de API centralizados como Axios se existirem. | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: D141 |
| D134 | O ConsolidacaoService possui o valor de 24 meses hardcoded em vários locais. | Pendente confirmado | **Descartado por critério** | —; origens duplicadas: D140, D143 |
| D135 | Manual usuarioId == null check instead of Spring Security filters in ConsolidacaoController. | Pendente confirmado | **Descartado por critério** | D033; origens duplicadas: D165 |
| D136 | O(N*M) loop instead of O(N) in ConsolidacaoService for 24-month projection. | Pendente confirmado | **Consolidado** | — |
| D137 | Redundant map lookups in ConsolidacaoService 24-month loop. | Pendente confirmado | **Consolidado** | — |
| D138 | BlocoCategorias loads independently causing layout shifts. | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D139 | Fetches all transactions regardless of requested period: Unfiltered Database Fetch fetches all transactions without date filtering at DB level. | Pendente confirmado | **Investigar — desempenho** | DW-139 — trabalho ativo; D162/A10; origens duplicadas: D142, D162, A10 |
| D140 | Hardcoded 24-month horizon magic numbers: Magic numbers used across frontend and backend. | Duplicado | **Duplicado — histórico** | D134 |
| D141 | Raw fetch and manual CSRF token instead of centralized client: Hook bypasses standard API clients. | Duplicado | **Duplicado — histórico** | D133 |
| D142 | Inefficient in-memory filtering for DespesaVariavel in ConsolidacaoService [ConsolidacaoService.java:165] — pre-existing query pattern across consolidacao services. | Duplicado | **Duplicado — histórico** | D139 |
| D143 | Hardcoded column count (24) in ConsolidacaoGrid.tsx — standard 24-month design layout for Epic 4. | Duplicado | **Duplicado — histórico** | D134 |
| D144 | Duplicated month abbreviations between ConsolidacaoPage and ConsolidacaoGrid — non-blocking cosmetic refactoring. | Obsoleto ou inaplicável | **Encerrado — inaplicável** | — |
| D145 | Fragmented design system colors in ConsolidacaoPage.tsx — pre-existing hex usage in pages. | Obsoleto ou inaplicável | **Encerrado — inaplicável** | — |
| D146 | Divergência na agregação entre contas e categorias para lançamentos com conta nula [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:140] — pre-existing logic in category consolidation where null accounts are assigned to synthetic 'Sem Categoria'. | Já resolvido, com evidência | **Resolvido — histórico** | A09 |
| D147 | Otimizar consultas de listagem com carregamento antecipado (JOIN FETCH) para evitar consultas N+1 na conversão de DTOs [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java:35] — pre-existing lazy fetch pattern. | Necessita esclarecimento ou validação adicional | **Investigar — desempenho** | DW-147 — trabalho ativo; — |
| D148 | Extrair cálculo de vigência temporal de parcelas em helper compartilhado [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:46] — duplicado entre DespesaVariavelService e ReceitaVariavelService. | Pendente confirmado | **Descartado por critério** | D114/D173 |
| D149 | Normalizar retorno genérico ResponseEntity<?> para ResponseEntity<*ResponseDTO> via RestControllerAdvice na Story 5.2 [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:40] — dependente da Story 5.2. | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D150 | Alinhar anotação de valor máximo @Max(9999999) na entidade JPA com @DecimalMax(9999999.99) do DTO [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:46] — pre-existing entity annotation. | Pendente confirmado | **Descartado por critério** | — |
| D151 | Adicionar ordenação alfabética (OrderByDescricaoAsc) na listagem de Contas e Categorias [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:28] — pre-existing query pattern. | Pendente confirmado | **Descartado por critério** | D158; origens duplicadas: D158 |
| D152 | Centralize duplicate validarEObterConta and validarEObterCategoria across transaction services into shared validation component | Pendente confirmado | **Descartado por critério** | — |
| D153 | Optimize foreign key validation queries in transaction services to avoid two roundtrips (findOwnerUsuarioId + findById) | Pendente confirmado | **Investigar — desempenho** | DW-153 — trabalho ativo; — |
| D154 | Padronizar payload de erro com ApiErrorResponse na exclusão de recurso inexistente (DELETE /{id}) | Pendente confirmado | **Descartado por critério** | D174; origens duplicadas: D174 |
| D155 | Criar índices adicionais no banco para chaves estrangeiras (conta_id, categoria_id) e usuario_id em contas/categorias | Pendente confirmado | **Investigar — desempenho** | DW-155 — trabalho ativo; A13/D161 |
| D156 | Alinhar consistência de cálculo histórico no ConsolidacaoService para movimentações sem dataInicio | Necessita esclarecimento ou validação adicional | **Descartado por critério** | — |
| D157 | Padronizar tratamento de erros dos filtros de segurança (Spring Security) no formato ApiErrorResponse | Pendente confirmado | **Descartado por critério** | D033 |
| D158 | Aplicar ordenação alfabética (OrderByDescricaoAsc) em CategoriaService.listar() | Duplicado | **Duplicado — histórico** | D151 |
| D159 | Padronizar retorno dos métodos criar entre controllers (ResponseEntity vs DTO direto) | Pendente confirmado | **Descartado por critério** | — |
| D160 | Tratar fail-closed estrito para tenant não autenticado no CurrentTenantIdentifierResolverImpl | Necessita esclarecimento ou validação adicional | **Investigar — segurança** | DW-160 — trabalho ativo; D037/D038 |
| D161 | Consolidar índice B-tree duplicado em usuarios(google_subject_id) | Pendente confirmado | **Planejar — condicional** | DW-161 — trabalho ativo; A13/D155 |
| D162 | Otimizar consultas do ConsolidacaoService para utilizar os índices de data diretamente no banco via JPQL/SQL ao invés de carga em memória | Duplicado | **Duplicado — histórico** | D139 |
| D163 | Configurar isolamento de banco de dados/Testcontainers ou profile específico para BackendApplicationTests sem exigir container Postgres ativo na porta 5432 | Já resolvido, com evidência | **Resolvido — histórico** | —; origens duplicadas: A12 |
| D164 | Adicionar testes MockMvc verificando HTTP 403 Forbidden nos controllers de movimentação quando tenant for inválido | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D165 | Eliminar checagem manual de autenticação (usuarioId == null) em ConsolidacaoController delegando para o filtro Spring Security | Duplicado | **Duplicado — histórico** | D135 |
| D166 | Utilizar a coluna persistida dataFim no ConsolidacaoService ao invés de recalcular a data final das parcelas em memória | Pendente confirmado | **Consolidado** | — |
| D167 | Coexistência de hibernate.ddl-auto: update e migrações Flyway em application.yaml | Pendente confirmado | **Descartado por critério** | — |
| D168 | Integrar ErrorBoundary com QueryErrorResetBoundary do TanStack Query para limpeza automática de queries em cache no retry | Pendente confirmado | **Descartado por critério** | A14; origens duplicadas: A14 |
| D169 | Tratar entrada com formato inválido de ano e mês no parsing de selectedMonth em ConsolidacaoToolbar | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D170 | Exibir estado de loading/erro para contas e categorias em ContaCategoriaFields [frontend/src/components/MovimentacaoForm/ContaCategoriaFields.tsx:30] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | — |
| D171 | Adicionar contador visual de caracteres (0/300) em ObservacoesField [frontend/src/components/MovimentacaoForm/ObservacoesField.tsx:37] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | — |
| D172 | Configuração do maven-failsafe-plugin e execução de testes de integração com Testcontainers [backend-api/pom.xml:149] — resolved in Story 6.2 | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D173 | Extração de abstração/interface comum de parcelamento para DespesaVariavel e ReceitaVariavel [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | — |
| D174 | Padronizar retorno de 404 em ContaController.excluir para retornar ApiErrorResponse estruturado via ResourceNotFoundException [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:58] — deferred, pre-existing | Duplicado | **Duplicado — histórico** | D154 |
| D175 | Implementar endpoints REST de busca por ID (GET /{id}) para as demais entidades de domínio (Categoria, Despesa e Receita) [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | — |
| D176 | Expansão exaustiva de testes de DTOs, CSRF em verbos PUT/DELETE e integridade com outras entidades financeiras [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/] — deferred, pre-existing | Necessita esclarecimento ou validação adicional | **Manter — segurança** | DW-176 — trabalho ativo; D122/D164 |
| D177 | Suporte a tecla Escape e semântica WAI-ARIA (aria-haspopup, role="menu", role="menuitem") no componente ActionMenu [frontend-web/src/components/ui/ActionMenu.tsx:45] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | — |
| D178 | Unificação do componente DeleteConfirmModal compartilhado entre as páginas em vez de implementações duplicadas [frontend-web/src/pages/] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | — |
| D179 | Ajuste das dependências do useEffect (deleteMutation) em DeleteConfirmModal para eliminar warning do linter react-hooks/exhaustive-deps [frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:18 e frontend-web/src/pages/ReceitasVariaveis/components/DeleteConfirmModal.tsx:14] — deferred, pre-existing | Já resolvido, com evidência | **Resolvido — histórico** | — |
| D180 | Suíte de testes unitários para ContaFormModal e submissão em ContasPage [frontend-web/src/pages/Contas/components/ContaFormModal.tsx:1] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | D072 |
| D181 | Suporte a flags para validação seletiva/modular (--frontend e --backend) no script de validação [scripts/validate.sh:65] — deferred, improvement | Pendente confirmado | **Descartado por critério** | — |
| D182 | Configuração de ferramenta de análise estática e linter no backend (Checkstyle, Spotless ou SpotBugs no Maven) [backend-api/pom.xml:153] — deferred, pre-existing | Pendente confirmado | **Descartado por critério** | — |
| D183 | Otimizar execução do modo --with-integration no script de validação para evitar execução duplicada da suíte unitária [scripts/validate.sh:172] — deferred, improvement | Pendente confirmado | **Descartado por critério** | — |
| D184 | Avaliar introdução de ferramentas de cobertura quantitativa de testes (JaCoCo no Maven e cobertura Vitest no frontend) pós-V1 [docs/architecture/testing.md:120] — deferred, future | Pendente confirmado | **Descartado por critério** | — |
| D185 | Distinguir consultas pausadas sem dados de cadastros vazios. | Pendente confirmado | **Descartado por critério** | — |
| D186 | Indicar recuperação em andamento e evitar cliques repetidos. | Pendente confirmado | **Descartado por critério** | — |
| D187 | Retirar console.error da renderização de RequiresDependencies. | Pendente confirmado | **Descartado por critério** | — |
| D188 | Melhorar anúncios e hierarquia de títulos dos estados de dependências. | Pendente confirmado | **Descartado por critério** | D099/D100 |
| D189 | Completar retorno ao fluxo original após cadastrar dependências. | Pendente confirmado | **Descartado por critério** | — |
| D190 | Avaliar abertura direta dos formulários nos links Cadastrar Conta e Cadastrar Categoria. | Pendente confirmado | **Descartado por critério** | — |

## Transcrição numerada do deferred-work anterior à organização

Esta seção é evidência histórica, não backlog ativo. Prefixos de citação impedem que os blocos antigos sejam confundidos com novas entradas abertas. O número após `>` é a linha física original usada pela triagem; separadores `\n` literais foram preservados.

SHA-256 do conteúdo original, antes de adicionar os prefixos de citação: `ed7d4d1eaaba588044726181c84c210853d5f68de1927a27eb2c39200c9f4304`.

> 0001 | - source_spec: `none`
> 0002 |   summary: Regenerate aureus-erd.png
> 0003 |   evidence: The static image is out of sync with the updated DBML after adding OAuth2 fields.
> 0004 | - source_spec: `none`
> 0005 |   summary: Add session/refresh token entity to data and domain models
> 0006 |   evidence: Required for full OAuth2 implementation, but out of scope for the immediate structural sync.
> 0007 | - source_spec: `none`
> 0008 |   summary: Add audit attributes to domain classes (Conta, Categoria, Despesa, Receita)
> 0009 |   evidence: These classes are missing criadoEm/atualizadoEm which exist in the DBML.
> 0010 | - source_spec: `none`
> 0011 |   summary: Sync Despesa attributes between class diagram and DBML
> 0012 |   evidence: Class diagram is missing localCompra, dataCompra, and observacoes that are defined in DBML.
> 0013 | - source_spec: `none`
> 0014 |   summary: Sync Receita attributes between class diagram and DBML
> 0015 |   evidence: Class diagram is missing observacoes defined in DBML.
> 0016 | - source_spec: `none`
> 0017 |   summary: Resolve structural discrepancy for TipoMovimento
> 0018 |   evidence: DBML stores fixed/variable directly on transactions, while class diagram abstracts it via ContextoFinanceiro.
> 0019 | - source_spec: none
> 0020 |   summary: Story 1.2 - Autenticação via Google (OAuth 2.0) e Tratamento de Erros
> 0021 |   evidence: Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
> 0022 | - source_spec: none
> 0023 |   summary: Story 1.3 - Proteção de Sessão e Isolamento de Dados
> 0024 |   evidence: Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
> 0025 | - source_spec: none
> 0026 |   summary: Story 1.4 - Encerrar Sessão (Logout) e Limpeza de Estado
> 0027 |   evidence: Separado do Epic 1 completo para manter o foco na criação do esqueleto visual primeiro.
> 0028 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-1-esqueleto-visual-barra-navegacao.md`
> 0029 |   summary: Add Navigation icons
> 0030 |   evidence: The UX design requires a bottom navigation bar with 5 icons, but the current implementation lacks icons.
> 0031 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-1-esqueleto-visual-barra-navegacao.md`
> 0032 |   summary: Add semantic states colors to CSS tokens
> 0033 |   evidence: Essential color tokens such as text colors, secondary backgrounds, and semantic states (success/error) are missing.
> 0034 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-1-esqueleto-visual-barra-navegacao.md`
> 0035 |   summary: Add automated tests for client-side routing
> 0036 |   evidence: Unverified navigation link paths and client-side routing paths without component integration tests.
> 0037 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
> 0038 |   summary: Fix permissive authorization fallback and protect Actuator endpoints
> 0039 |   evidence: SecurityConfig currently uses `.anyRequest().permitAll()`, which inadvertently exposes unmatched endpoints, including Spring Boot Actuator, without authentication.
> 0040 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
> 0041 |   summary: Implement backend unit tests for AuthController
> 0042 |   evidence: Review caught that the logout endpoint lacks test coverage.
> 0043 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
> 0044 |   summary: Implement frontend unit tests for Navigation component
> 0045 |   evidence: Review caught that the handleLogout logic in the frontend lacks test coverage.
> 0046 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
> 0047 |   summary: Configure global defaults for React QueryClient
> 0048 |   evidence: The QueryClient in main.tsx uses out-of-the-box settings, missing optimized defaults like staleTime and refetchOnWindowFocus.
> 0049 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-encerrar-sessao.md`
> 0050 |   summary: Configure secure attribute for session cookie based on environment
> 0051 |   evidence: AuthController hardcodes secure(false) for the AUREUS_SESSION cookie, which must be enabled dynamically for production.
> 0052 |
> 0053 | ## Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
> 0054 | - Interface de carregamento (Loading) não estlizada [frontend/src/App.tsx]
> 0055 | - Redirecionamento de login não preserva o estado de rota prévia (`location.state`) [frontend/src/App.tsx:47]
> 0056 | - Rotas hardcoded ao invés de usar constantes centralizadas [frontend/src/App.tsx]
> 0057 | - Ausência de Error Boundary genérico para falhas do React [frontend/src/App.tsx]
> 0058 | - Ausência de testes end-to-end e componentes para o Auth Fetch e Logout
> 0059 | - Ausência de verificação contra bypass de rota protegida
> 0060 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
> 0061 |   summary: Missing verification for Google profile picture extraction
> 0062 |   evidence: No assertion checks that the picture attribute from OAuth2User is correctly mapped and saved to the user entity.
> 0063 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
> 0064 |   summary: Missing verification for profile picture inclusion in /me response
> 0065 |   evidence: No test covers this endpoint or its consumption in the frontend.
> 0066 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
> 0067 |   summary: Missing verification for Header logout and profile UI
> 0068 |   evidence: No frontend test checks that Header displays the user profile image or that logout triggers the flow.
> 0069 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
> 0070 |   summary: Unnecessary Database Writes on Login
> 0071 |   evidence: OAuth2LoginSuccessHandler saves the user entity unconditionally on every login.
> 0072 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
> 0073 |   summary: Inefficient Profile Fetching
> 0074 |   evidence: AuthController.me() queries the database on every check.
> 0075 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
> 0076 |   summary: Direct Repository Access in AuthController
> 0077 |   evidence: Bypasses the service layer.
> 0078 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-4-correcao-botao-sair.md`
> 0079 |   summary: Missing Strongly Typed DTO in AuthController
> 0080 |   evidence: /api/auth/me endpoint returns a loosely typed Map<String, Object>.
> 0081 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
> 0082 |   summary: Hardcoded frontend redirect URI in OAuth2LoginSuccessHandler.
> 0083 |   evidence: The URL `http://localhost:5173/` is hardcoded instead of being loaded from application properties.
> 0084 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
> 0085 |   summary: Overly broad exception catching in JwtAuthenticationFilter.
> 0086 |   evidence: The filter catches generic `Exception` instead of specific JWT exceptions, potentially hiding system errors.
> 0087 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
> 0088 |   summary: Empty Response Body on 401 Unauthorized in JwtAuthenticationFilter.
> 0089 |   evidence: The filter sets status 401 but writes no JSON body, making frontend error handling harder.
> 0090 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
> 0091 |   summary: Potential Token and Cookie Expiration Mismatch.
> 0092 |   evidence: The `AUREUS_SESSION` cookie uses a hardcoded 86400 maxAge which might drift from the JWT expiration property.
> 0093 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
> 0094 |   summary: Missing verification for Tenant context population.
> 0095 |   evidence: There are no unit or integration tests verifying that `JwtAuthenticationFilter` populates and clears the `TenantContext`.
> 0096 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
> 0097 |   summary: Missing verification for Session cookie SameSite security attribute.
> 0098 |   evidence: There are no tests verifying that `OAuth2LoginSuccessHandler` emits a cookie with `SameSite=Lax`.
> 0099 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md`
> 0100 |   summary: Missing verification for Hibernate tenant resolution.
> 0101 |   evidence: There are no tests verifying that `CurrentTenantIdentifierResolverImpl` correctly returns the value from `TenantContext`.
> 0102 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0103 |   summary: Loss of Spring Context in Tenant Resolver
> 0104 |   evidence: application.yaml changed tenant_identifier_resolver to a fully qualified class name, bypassing Spring context.
> 0105 |
> 0106 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0107 |   summary: Security Vulnerability via URL Token Transmission
> 0108 |   evidence: OAuth2LoginSuccessHandler removed Cookie import, potentially exposing JWT.
> 0109 |
> 0110 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0111 |   summary: Missing JWT Validation Error Handling
> 0112 |   evidence: JwtUtil removed JwtException catch, potentially leading to 500s instead of 401s.
> 0113 |
> 0114 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0115 |   summary: Unrecoverable Profile Image Error State
> 0116 |   evidence: Header.tsx uses setImgError(true) but does not reset it if the profileImage prop changes.
> 0117 |
> 0118 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0119 |   summary: Hardcoded Avatar Fallback
> 0120 |   evidence: Header.tsx renders a hardcoded "U" instead of a dynamic initial.
> 0121 |
> 0122 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0123 |   summary: Non-Semantic DOM Structure
> 0124 |   evidence: Header.tsx includes an empty div for header-left just to satisfy CSS grid.
> 0125 |
> 0126 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0127 |   summary: Tenant identifier resolver configuration lacks verification
> 0128 |   evidence: No tests exist to assert tenant context is actively resolved and applied.
> 0129 |
> 0130 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0131 |   summary: Profile image referrer policy is unverified
> 0132 |   evidence: No component tests check that the img tag receives the referrerPolicy attribute.
> 0133 |
> 0134 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-navbar-e-logout.md`
> 0135 |   summary: Application navigation layout composition is unverified
> 0136 |   evidence: No tests exist to verify that Navigation is properly rendered within Header.
> 0137 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0138 |   summary: OAuth2LoginSuccessHandler Cookie import removed
> 0139 |   evidence: Bypassing standard cookie manipulation or HTTP-only setup for JWTs raises transmission security concerns.
> 0140 |
> 0141 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0142 |   summary: OAuth2LoginSuccessHandler missing audit logging
> 0143 |   evidence: Successful authentications do not update a last_login timestamp or record audit events.
> 0144 |
> 0145 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0146 |   summary: OAuth2LoginSuccessHandler missing error handling
> 0147 |   evidence: Missing logic for when OAuth2 providers return incomplete profiles without required claims.
> 0148 |
> 0149 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0150 |   summary: UsuarioRepository missing @Repository annotation
> 0151 |   evidence: Removal of this annotation disables automatic persistence exception translation.
> 0152 |
> 0153 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0154 |   summary: UsuarioRepository missing google_subject_id index
> 0155 |   evidence: findByGoogleSubjectId is on the critical login path, but there's no DB migration creating an index for it.
> 0156 |
> 0157 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0158 |   summary: JwtUtil JwtException import removed
> 0159 |   evidence: Suggests explicit handling of token validation errors like expiration or malformed signatures was removed.
> 0160 |
> 0161 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0162 |   summary: Tenant isolation missing tests
> 0163 |   evidence: multitenancy setup in application.yaml lacks test verification for cross-tenant data isolation.
> 0164 |
> 0165 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0166 |   summary: application.yaml OAuth2 hardcoded secrets risk
> 0167 |   evidence: security.oauth2.client block does not clearly use placeholder variables for secrets.
> 0168 |
> 0169 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-header-transparente-flutuante.md`
> 0170 |   summary: Header CSS hardcoded z-index
> 0171 |   evidence: z-index: 100 used without a centralized scale, risking overlaps with other components.
> 0172 |
> 0173 | ## Deferred from: code review of spec-1-1-esqueleto-visual-barra-navegacao.md (2026-08-18)
> 0174 | - Entire React application unmounts on route rendering error [frontend/src/App.tsx:68]
> 0175 |
> 0176 | ## Deferred from: code review of spec-1-2-autenticacao-via-google-oauth-2-0-e-tratamento-de-erros.md (2026-08-18)
> 0177 | - Missing Backend Logout Endpoint (belongs to Story 1.4)
> 0178 | - Incomplete CSRF Setup on Frontend
> 0179 | - Missing automated tests for auth flows
> 0180 | - Missing frontend interception of 401 errors during API access
> 0181 | - Concurrent OAuth logins for same new user could cause DataIntegrityViolationException
> 0182 |
> 0183 | ## Deferred from: code review of spec-1-3-protecao-de-sessao-e-isolamento-de-dados.md (2026-08-18)
> 0184 | - Missing Automated Tests
> 0185 | - Lack of Asynchronous Thread Context Propagation
> 0186 |
> 0187 | ## Deferred from: code review of spec-1-4-encerrar-sessao.md (2026-08-18)
> 0188 | - Vazamento de contexto no TenantContext: Usando ThreadLocal padrão.
> 0189 | - Omissão de acessibilidade no dropdown do Header (Esc): Falta listener para a tecla Escape.
> 0190 | - Preenchimento extra no mobile para rodapé: O .main-content tem padding inferior de 80px herdado da antiga navegação.
> 0191 | - Ausência de testes de integração e endpoints backend: verification-gap acusou falta de testes para controllers e filtros criados.
> 0192 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
> 0193 |   summary: Adicionar testes de navegação para certificar que rotas não-raiz renderizam o conteúdo esperado.
> 0194 |   evidence: Surfaced by verification-gap review; App.test.tsx apenas verifica a rota /.
> 0195 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
> 0196 |   summary: Adicionar teste automatizado E2E ou de artefatos para verificar se os estilos do Tailwind são processados e aplicados com sucesso na build.
> 0197 |   evidence: Surfaced by verification-gap review; O projeto atual não valida o CSS final processado.
> 0198 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
> 0199 |   summary: Corrigir a11y: adicionar aria-live e role="status" ao estado de carregamento e aria-controls ao botão de perfil.
> 0200 |   evidence: Surfaced by blind-hunter review.
> 0201 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-1/spec-correcao-uso-tailwind.md`
> 0202 |   summary: Utilizar a inicial do usuário logado no avatar padrão em vez de 'U' fixo.
> 0203 |   evidence: Surfaced by blind-hunter review.
> 0204 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
> 0205 |   summary: Adicionar testes de unidade e integração (Backend e Frontend) para Contas
> 0206 |   evidence: Nenhuma cobertura de teste foi adicionada na implementação da story 2.1.
> 0207 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
> 0208 |   summary: Adicionar métodos equals() e hashCode() na entidade Conta
> 0209 |   evidence: Conta não possui implementação customizada recomendada pelo JPA.
> 0210 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
> 0211 |   summary: Implementar Focus Trap acessível e completo no Modal
> 0212 |   evidence: Componente genérico Modal.tsx não possui restrição de foco para teclado.
> 0213 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
> 0214 |   summary: Adicionar suporte à paginação no backend e listagem de Contas
> 0215 |   evidence: O endpoint /api/contas retorna todos os registros de uma vez.
> 0216 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md`
> 0217 |   summary: Adicionar tratamento e exibição de erro da API com parsing estruturado no frontend
> 0218 |   evidence: useContas.ts lança erros genéricos sem extrair mensagens do body de erro.
> 0219 |
> 0220 | ## Deferred from: code review (2026-08-19) - code review of spec-2-1-gestao-de-contas-crud-com-protecao-de-vinculo.md
> 0221 | - Endpoint criar Conta retorna 200 OK em vez de 201 Created.
> 0222 | - Modal genérico não possui Focus Trap acessível.
> 0223 | - Tratamento de erros de validação server-side não mapeados no formulário.
> 0224 | - Endpoint de listagem de contas não é paginado.
> 0225 | - Entidade Conta sem implementação customizada de equals() e hashCode().
> 0226 | - Modal não se adapta dinamicamente para Bottom Sheet em mobile.
> 0227 | - Ausência de testes verificando isolamento cross-tenant e invalidação de cache.
> 0228 | - Ausência de testes para contrato de violação de FK na deleção.
> 0229 |
> 0230 | ## Deferred from: code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
> 0231 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
> 0232 |   summary: Endpoint criar Categoria retorna 200 OK em vez de 201 Created.
> 0233 |   evidence: Pre-existing pattern from Conta Controller; low impact for now.
> 0234 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
> 0235 |   summary: Entidade Categoria sem métodos equals() e hashCode().
> 0236 |   evidence: Pre-existing pattern; low impact for current detached entity usage.
> 0237 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
> 0238 |   summary: O endpoint de exclusão assume 400 apenas como FK violation.
> 0239 |   evidence: Out of scope to overhaul error parsing; validation details might be masked.
> 0240 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
> 0241 |   summary: Listagem de Categorias não possui paginação.
> 0242 |   evidence: Frontend and backend fetch all records at once, risky for large tenants.
> 0243 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
> 0244 |   summary: Falta de tratamento de timeout para requisições frontend.
> 0245 |   evidence: Network hangs indefinitely instead of aborting.
> 0246 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-2/spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md`
> 0247 |   summary: Testes automatizados ausentes para Categoria.
> 0248 |   evidence: Multi-tenant isolation, FK constraint errors, and frontend cache invalidation have no automated tests.
> 0249 |
> 0250 | ## Deferred from: code review (2026-08-20) - code review of spec-2-2-gestao-de-categorias-crud-com-protecao-de-vinculo.md
> 0251 | - Missing Location header in Categoria creation.
> 0252 | - useCategorias.ts lacks AbortSignal.
> 0253 | - Optimistic locking (missing @Version).
> 0254 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
> 0255 |   summary: Improve UX by checking both accounts and categories simultaneously to avoid a "waterfall" of empty states.
> 0256 |   evidence: A user missing both will first see the warning for accounts, fix it, and then unexpectedly face a second block for categories.
> 0257 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
> 0258 |   summary: Add data-testid attributes to loading, error, and empty states.
> 0259 |   evidence: Automated testing will be brittle without semantic data-testid attributes.
> 0260 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
> 0261 |   summary: Add a Retry button to the error state.
> 0262 |   evidence: Currently, the user is forced to manually refresh the entire application if the initial fetch fails.
> 0263 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
> 0264 |   summary: Improve error state UI to match the polished, card-based layouts of the empty states.
> 0265 |   evidence: The error state UI is a simple line of text, which feels visually inconsistent.
> 0266 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-2/spec-2-3-restricao-granular-de-cadastro-sem-dependencias.md`
> 0267 |   summary: Add automated tests to verify dependency guards block access to transaction routes.
> 0268 |   evidence: The test suite would not fail if a developer removed the RequiresDependencies wrapper, allowing users to access transaction pages without required data.
> 0269 |
> 0270 | ## Deferred from: code review of spec-2-3-restricao-granular-de-cadastro-sem-dependencias (2026-08-20)
> 0271 | - Missing `role="status"` on loading state.
> 0272 | - Missing `role="alert"` on error state.
> 0273 | - Missing `data-testid` attributes on conditional UI states.
> 0274 | - Repetitive `<ProtectedRoute><RequiresDependencies>` wrappers in App.tsx.
> 0275 | - Scope creep / missing `aria-hidden="true"`: Unrequested additions of `lucide-react` icons in Header.tsx.
> 0276 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
> 0277 |   summary: Missing Relationship Ownership Validation (verify if related conta/categoria belong to user).
> 0278 |   evidence: Not specific to this story, applies globally to existing entities too.
> 0279 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
> 0280 |   summary: Missing Pagination for /api/despesas-fixas and /api/receitas-fixas.
> 0281 |   evidence: Table grows large over time, missing in other controllers as well.
> 0282 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
> 0283 |   summary: Clarify if fixed items need an end date (dataFim) or active flag to prevent history mutation on deletion.
> 0284 |   evidence: Currently deletion removes the item entirely, which might break historical monthly projections. Needs product definition.
> 0285 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
> 0286 |   summary: Missing backend integration tests for new endpoints.
> 0287 |   evidence: Project currently lacks test infrastructure, deferring per Epic 1 retro items.
> 0288 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md`
> 0289 |   summary: Missing frontend component tests for DespesasFixasPage and ReceitasFixasPage.
> 0290 |   evidence: Project currently lacks test infrastructure, deferring per Epic 1 retro items.
> 0291 |
> 0292 | ## Deferred from: code review (2026-08-20) spec-3-1-lancamentos-fixos-despesas-e-receitas-com-vigencia.md
> 0293 | - Falta de validação de propriedade (Tenant) nos relacionamentos informados (conta/categoria)
> 0294 | - Mutação de histórico financeiro em exclusões (falta de dataFim/soft-delete)
> 0295 | - Falta de paginação nos endpoints `GET` de listagem
> 0296 | - Falta de cobertura de testes automatizados (integração e UI)
> 0297 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0298 |   summary: Controllers return JPA Entities directly instead of using DTOs, risking Mass Assignment
> 0299 |   evidence: Found in DespesaVariavelController and ReceitaVariavelController (Pre-existing project pattern)
> 0300 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0301 |   summary: Duplicated date logic (preencherDataFim) in controllers instead of centralizing in service or lifecycle hook
> 0302 |   evidence: Found repeated in both variable controllers
> 0303 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0304 |   summary: Missing DB indexes for dataInicio and dataFim which will be heavily queried in Epic 4
> 0305 |   evidence: Not explicitly requested in the spec, but necessary for future performance
> 0306 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0307 |   summary: Missing class-level validation to ensure dataFim >= dataInicio
> 0308 |   evidence: Although the controller calculates it, programmatic API changes could introduce inconsistencies
> 0309 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0310 |   summary: Unbounded findAll queries in backend list endpoints
> 0311 |   evidence: Pre-existing technical debt across the project (Epic 2, etc.)
> 0312 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0313 |   summary: DRY violation in frontend form/list pages for variable expenses and revenues
> 0314 |   evidence: Boilerplate is duplicated between the two modules
> 0315 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0316 |   summary: Missing Auditing fields (createdAt, updatedAt) across entities
> 0317 |   evidence: Technical debt from prior architecture setup
> 0318 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0319 |   summary: Lack of global @ControllerAdvice for standard constraint violations
> 0320 |   evidence: Pre-existing pattern where controllers individually catch DataIntegrityViolationException
> 0321 | - source_spec: `/home/guilhermepagio/developer/workspace/projeto-aureus/_bmad-output/implementation-artifacts/archive/epic-3/spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md`
> 0322 |   summary: Concurrent deletion causes unhandled 500 EmptyResultDataAccessException
> 0323 |   evidence: Pre-existing basic CRUD pattern does not handle race conditions gracefully
> 0324 |
> 0325 | ## Deferred from: code review of spec-3-2-lancamentos-variaveis-e-parcelados-com-arredondamento-de-centavos.md (2026-08-21)
> 0326 | - Missing automated verification tests (dataFim, relational errors, API parsing)
> 0327 | - Missing DTOs, Service Layer, and Global Exception Handler
> 0328 | - Fetcher swallows non-JSON 500 errors
> 0329 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
> 0330 |   summary: Fixed transactions lack start/end dates in the domain model, preventing month-based filtering.
> 0331 |   evidence: Fixed transaction components had to bypass the month filtering logic completely because `dataInicio` does not exist on the current entity.
> 0332 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
> 0333 |   summary: Missing test coverage for React Query mutation cache invalidations and global month filter logic.
> 0334 |   evidence: No component or hook tests assert that `['consolidacao']` is invalidated or that `isGlobalFilterActive` works correctly.
> 0335 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
> 0336 |   summary: `epic-3-context.md` was translated to English, losing technical specificity and causing context fragmentation with Portuguese PRDs.
> 0337 |   evidence: The document omits explicit constraints like `ON DELETE RESTRICT` for foreign keys that were present in the Portuguese original.
> 0338 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
> 0339 |   summary: monthStore currentMonth is initialized at module load and can become stale.
> 0340 |   evidence: initialized at top-level instead of inside the store or getter.
> 0341 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
> 0342 |   summary: No UI exists to change the selected month.
> 0343 |   evidence: month picker belongs to Epic 4 (Seleção de mês).
> 0344 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
> 0345 |   summary: Duplicated toggle switch and SVG icon markup.
> 0346 |   evidence: identical markup across pages instead of reusable UI components.
> 0347 | - source_spec: `_bmad-output/implementation-artifacts/archive/epic-3/spec-3-4-sincronizacao-de-visao-mensal-invalidacao-de-cache-e-filtro.md`
> 0348 |   summary: useContas and useCategorias do not invalidate ['consolidacao'].
> 0349 |   evidence: deleting accounts/categories could affect consolidation cache but wasn't updated here.
> 0350 | - source_spec: `_bmad-output/implementation-artifacts/spec-4-1-selecao-de-mes-navegacao-temporal-e-grid-com-suporte-a-gesto.md`
> 0351 |   summary: Adicionar testes de unidade para ConsolidacaoPage, ConsolidacaoGrid e ConsolidacaoToolbar garantindo que as 24 colunas são renderizadas e o seletor funciona.
> 0352 |   evidence: Foi identificado que `App.test.tsx` apenas testa o título, e a nova navegação de meses do Painel de Consolidação ficou sem testes de regressão de renderização.
> 0353 | \n- source_spec: `_bmad-output/implementation-artifacts/spec-4-2-bloco-de-consolidacao-por-conta-receitas-e-despesas.md`\n  summary: O hook useConsolidacao utiliza fetch diretamente, possivelmente contornando clientes de API centralizados como Axios se existirem.\n  evidence: O código usa window.fetch e extrai CSRF token manualmente.\n- source_spec: `_bmad-output/implementation-artifacts/spec-4-2-bloco-de-consolidacao-por-conta-receitas-e-despesas.md`\n  summary: O ConsolidacaoService possui o valor de 24 meses hardcoded em vários locais.\n  evidence: Loops e ArrayLists inicializados com capacidade 24 explicitamente.
> 0354 | - source_spec: `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
> 0355 |   summary: Manual usuarioId == null check instead of Spring Security filters in ConsolidacaoController.
> 0356 |   evidence: Controller manually rejects unauthorized access.
> 0357 | - source_spec: `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
> 0358 |   summary: O(N*M) loop instead of O(N) in ConsolidacaoService for 24-month projection.
> 0359 |   evidence: The logic iterates through all transactions 24 times instead of calculating index offsets.
> 0360 | - source_spec: `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
> 0361 |   summary: Redundant map lookups in ConsolidacaoService 24-month loop.
> 0362 |   evidence: start and end dates are repeatedly retrieved from maps for every transaction inside the inner loop.
> 0363 | - source_spec: `_bmad-output/implementation-artifacts/spec-4-3-bloco-analitico-de-categorias.md`
> 0364 |   summary: BlocoCategorias loads independently causing layout shifts.
> 0365 |   evidence: The block is rendered immediately and manages its own loading state below accounts, causing visual snap.
> 0366 |
> 0367 | ## Deferred from: code review (spec-4-3-bloco-analitico-de-categorias.md)
> 0368 | - Fetches all transactions regardless of requested period: Unfiltered Database Fetch fetches all transactions without date filtering at DB level.
> 0369 | - Hardcoded 24-month horizon magic numbers: Magic numbers used across frontend and backend.
> 0370 | - Raw fetch and manual CSRF token instead of centralized client: Hook bypasses standard API clients.
> 0371 |
> 0372 | ## Deferred from: code review of spec-4-3-bloco-analitico-de-categorias.md (2026-09-05)
> 0373 |
> 0374 | - Inefficient in-memory filtering for DespesaVariavel in ConsolidacaoService [ConsolidacaoService.java:165] — pre-existing query pattern across consolidacao services.
> 0375 | - Hardcoded column count (24) in ConsolidacaoGrid.tsx — standard 24-month design layout for Epic 4.
> 0376 | - Duplicated month abbreviations between ConsolidacaoPage and ConsolidacaoGrid — non-blocking cosmetic refactoring.
> 0377 | - Fragmented design system colors in ConsolidacaoPage.tsx — pre-existing hex usage in pages.
> 0378 |
> 0379 | ## Deferred from: code review of spec-4-4-bloco-de-resumo-geral-com-sobra-historica-acumulada.md (2026-09-06)
> 0380 | - Divergência na agregação entre contas e categorias para lançamentos com conta nula [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:140] — pre-existing logic in category consolidation where null accounts are assigned to synthetic 'Sem Categoria'.
> 0381 |
> 0382 | ## Deferred from: code review of spec-5-1-backend-service-layer-e-dtos-records-para-entidades.md (2026-09-15)
> 0383 | - Otimizar consultas de listagem com carregamento antecipado (JOIN FETCH) para evitar consultas N+1 na conversão de DTOs [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java:35] — pre-existing lazy fetch pattern.
> 0384 | - Extrair cálculo de vigência temporal de parcelas em helper compartilhado [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java:46] — duplicado entre DespesaVariavelService e ReceitaVariavelService.
> 0385 | - Normalizar retorno genérico ResponseEntity<?> para ResponseEntity<*ResponseDTO> via RestControllerAdvice na Story 5.2 [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java:40] — dependente da Story 5.2.
> 0386 | - Alinhar anotação de valor máximo @Max(9999999) na entidade JPA com @DecimalMax(9999999.99) do DTO [backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java:46] — pre-existing entity annotation.
> 0387 | - Adicionar ordenação alfabética (OrderByDescricaoAsc) na listagem de Contas e Categorias [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java:28] — pre-existing query pattern.
> 0388 |
> 0389 | ## Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-16)
> 0390 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0391 |   summary: Centralize duplicate validarEObterConta and validarEObterCategoria across transaction services into shared validation component
> 0392 |   evidence: Review surfaced identical helper methods across all 4 transaction services
> 0393 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0394 |   summary: Optimize foreign key validation queries in transaction services to avoid two roundtrips (findOwnerUsuarioId + findById)
> 0395 |   evidence: Currently performs native check then findById, which could be combined
> 0396 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0397 |   summary: Padronizar payload de erro com ApiErrorResponse na exclusão de recurso inexistente (DELETE /{id})
> 0398 |   evidence: service.excluir(id) retorna false e controller responde ResponseEntity.notFound().build() sem body
> 0399 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0400 |   summary: Criar índices adicionais no banco para chaves estrangeiras (conta_id, categoria_id) e usuario_id em contas/categorias
> 0401 |   evidence: V1__create_performance_indexes.sql foca em datas e google_subject_id; chaves estrangeiras não indexadas causam seq scan em deletes
> 0402 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0403 |   summary: Alinhar consistência de cálculo histórico no ConsolidacaoService para movimentações sem dataInicio
> 0404 |   evidence: Transações sem dataInicio são incluídas na projeção futura de 24 meses mas ignoradas no saldo histórico pré-grade
> 0405 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0406 |   summary: Padronizar tratamento de erros dos filtros de segurança (Spring Security) no formato ApiErrorResponse
> 0407 |   evidence: 401 Unauthorized e 403 CSRF nos filtros de segurança são gerados antes do RestControllerAdvice
> 0408 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0409 |   summary: Aplicar ordenação alfabética (OrderByDescricaoAsc) em CategoriaService.listar()
> 0410 |   evidence: CategoriaService executa findAll() em vez do método ordenado do repositório
> 0411 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0412 |   summary: Padronizar retorno dos métodos criar entre controllers (ResponseEntity vs DTO direto)
> 0413 |   evidence: Movimentações retornam ResponseEntity<T> enquanto Conta e Categoria retornam DTO puro
> 0414 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0415 |   summary: Tratar fail-closed estrito para tenant não autenticado no CurrentTenantIdentifierResolverImpl
> 0416 |   evidence: Resolver retorna string 'public' quando contexto é nulo, com risco de persistência inadvertida se não autenticado
> 0417 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0418 |   summary: Consolidar índice B-tree duplicado em usuarios(google_subject_id)
> 0419 |   evidence: A coluna google_subject_id possui constraint UNIQUE na tabela usuarios (criando índice único automaticamente); um índice secundário não-único idx_usuarios_google_subject_id foi criado na migração V1.
> 0420 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0421 |   summary: Otimizar consultas do ConsolidacaoService para utilizar os índices de data diretamente no banco via JPQL/SQL ao invés de carga em memória
> 0422 |   evidence: findByUsuarioId carrega todo o histórico e calcula vigência de datas em laços Java na memória da aplicação, sem usar índices compostos para corte por data no banco.
> 0423 |
> 0424 |
> 0425 |
> 0426 |
> 0427 |
> 0428 | ## Deferred from: code review of spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md (2026-09-27)
> 0429 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0430 |   summary: Configurar isolamento de banco de dados/Testcontainers ou profile específico para BackendApplicationTests sem exigir container Postgres ativo na porta 5432
> 0431 |   evidence: Teste @SpringBootTest falha com conexão recusada se Postgres local não estiver ativo; alocado para infraestrutura de testes no Épico 6 (Story 6.2/6.4)
> 0432 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0433 |   summary: Adicionar testes MockMvc verificando HTTP 403 Forbidden nos controllers de movimentação quando tenant for inválido
> 0434 |   evidence: Isolamento de tenant testado na Service Layer e no ExceptionHandler; cobertura de integração REST de endpoints alocada na Story 6.2
> 0435 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0436 |   summary: Eliminar checagem manual de autenticação (usuarioId == null) em ConsolidacaoController delegando para o filtro Spring Security
> 0437 |   evidence: Padrão manual pré-existente do Épico 4 que retorna 401 vazio
> 0438 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0439 |   summary: Utilizar a coluna persistida dataFim no ConsolidacaoService ao invés de recalcular a data final das parcelas em memória
> 0440 |   evidence: ConsolidacaoService calcula data final em memória com plusMonths() em vez de ler a coluna dataFim indexada
> 0441 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-2-tratamento-global-erros-validacao-tenant-e-indices.md`
> 0442 |   summary: Coexistência de hibernate.ddl-auto: update e migrações Flyway em application.yaml
> 0443 |   evidence: Configuração de desenvolvimento permissiva que deve ser ajustada para validate em ambientes de produção/CI
> 0444 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-3-centralizacao-cliente-http-api-e-resiliencia-no-frontend.md`
> 0445 |   summary: Integrar ErrorBoundary com QueryErrorResetBoundary do TanStack Query para limpeza automática de queries em cache no retry
> 0446 |   evidence: Caso um erro de renderização seja provocado por dados inválidos em cache, o reset do ErrorBoundary sem reset de query pode reexecutar a renderização com o mesmo dado com erro.
> 0447 | - source_spec: `_bmad-output/implementation-artifacts/spec-5-4-abstracao-e-unificacao-dos-formularios-de-movimentacoes-financeiras.md`
> 0448 |   summary: Tratar entrada com formato inválido de ano e mês no parsing de selectedMonth em ConsolidacaoToolbar
> 0449 |   evidence: ConsolidacaoToolbar assume formato numérico válido ao quebrar a string selectedMonth por hífen sem validação de NaN
> 0450 |
> 0451 |
> 0452 | ## Deferred from: code review of spec-5-4-abstracao-e-unificacao-dos-formularios-de-movimentacoes-financeiras.md (2026-09-29)
> 0453 | - Exibir estado de loading/erro para contas e categorias em ContaCategoriaFields [frontend/src/components/MovimentacaoForm/ContaCategoriaFields.tsx:30] — deferred, pre-existing
> 0454 | - Adicionar contador visual de caracteres (0/300) em ObservacoesField [frontend/src/components/MovimentacaoForm/ObservacoesField.tsx:37] — deferred, pre-existing
> 0455 |
> 0456 | ## Deferred from: code review of spec-6-1-testes-unitarios-de-dominio-e-servicos-financeiros-backend.md (2026-10-04)
> 0457 | - Configuração do maven-failsafe-plugin e execução de testes de integração com Testcontainers [backend-api/pom.xml:149] — resolved in Story 6.2
> 0458 | - Extração de abstração/interface comum de parcelamento para DespesaVariavel e ReceitaVariavel [backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/] — deferred, pre-existing
> 0459 |
> 0460 | ## Deferred from: code review of spec-6-2-testes-de-integracao-rest-e-isolamento-multi-tenancy-backend.md (2026-10-05)
> 0461 | - Padronizar retorno de 404 em ContaController.excluir para retornar ApiErrorResponse estruturado via ResourceNotFoundException [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:58] — deferred, pre-existing
> 0462 | - Implementar endpoints REST de busca por ID (GET /{id}) para as demais entidades de domínio (Categoria, Despesa e Receita) [backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/] — deferred, pre-existing
> 0463 | - Expansão exaustiva de testes de DTOs, CSRF em verbos PUT/DELETE e integridade com outras entidades financeiras [backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/] — deferred, pre-existing
> 0464 |
> 0465 | ## Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-05)
> 0466 | - Suporte a tecla Escape e semântica WAI-ARIA (aria-haspopup, role="menu", role="menuitem") no componente ActionMenu [frontend-web/src/components/ui/ActionMenu.tsx:45] — deferred, pre-existing
> 0467 | - Unificação do componente DeleteConfirmModal compartilhado entre as páginas em vez de implementações duplicadas [frontend-web/src/pages/] — deferred, pre-existing
> 0468 | - Ajuste das dependências do useEffect (deleteMutation) em DeleteConfirmModal para eliminar warning do linter react-hooks/exhaustive-deps [frontend-web/src/pages/DespesasVariaveis/components/DeleteConfirmModal.tsx:18 e frontend-web/src/pages/ReceitasVariaveis/components/DeleteConfirmModal.tsx:14] — deferred, pre-existing
> 0469 |
> 0470 | ## Deferred from: code review of spec-6-3-testes-de-componentes-e-fluxos-criticos-no-frontend.md (2026-10-06)
> 0471 | - Suíte de testes unitários para ContaFormModal e submissão em ContasPage [frontend-web/src/pages/Contas/components/ContaFormModal.tsx:1] — deferred, pre-existing
> 0472 |
> 0473 | ## Deferred from: code review of spec-6-4-infraestrutura-de-validacao-continua-local-e-documentacao-canonica-de-testes.md (2026-10-06)
> 0474 | - Suporte a flags para validação seletiva/modular (--frontend e --backend) no script de validação [scripts/validate.sh:65] — deferred, improvement
> 0475 | - Configuração de ferramenta de análise estática e linter no backend (Checkstyle, Spotless ou SpotBugs no Maven) [backend-api/pom.xml:153] — deferred, pre-existing
> 0476 | - Otimizar execução do modo --with-integration no script de validação para evitar execução duplicada da suíte unitária [scripts/validate.sh:172] — deferred, improvement
> 0477 | - Avaliar introdução de ferramentas de cobertura quantitativa de testes (JaCoCo no Maven e cobertura Vitest no frontend) pós-V1 [docs/architecture/testing.md:120] — deferred, future
> 0478 |
> 0479 |
> 0480 |
> 0481 | - source_spec: archive/avulsas/2026-10/spec-padronizar-verde-receitas.md
> 0482 |   summary: Distinguir consultas pausadas sem dados de cadastros vazios.
> 0483 |   evidence: RequiresDependencies verifica isLoading, mas não trata estado pending/paused; preexistente.
> 0484 | - source_spec: archive/avulsas/2026-10/spec-padronizar-verde-receitas.md
> 0485 |   summary: Indicar recuperação em andamento e evitar cliques repetidos.
> 0486 |   evidence: Tentar novamente dispara refetch sem desabilitação nem indicação de isFetching; preexistente.
> 0487 | - source_spec: archive/avulsas/2026-10/spec-padronizar-verde-receitas.md
> 0488 |   summary: Retirar console.error da renderização de RequiresDependencies.
> 0489 |   evidence: O registro ocorre em cada renderização do ramo de falha; preexistente.
> 0490 | - source_spec: archive/avulsas/2026-10/spec-padronizar-verde-receitas.md
> 0491 |   summary: Melhorar anúncios e hierarquia de títulos dos estados de dependências.
> 0492 |   evidence: Carregamento e falha não têm status/alert e o primeiro título da área é h3; preexistente.
> 0493 | - source_spec: archive/avulsas/2026-10/spec-padronizar-verde-receitas.md
> 0494 |   summary: Completar retorno ao fluxo original após cadastrar dependências.
> 0495 |   evidence: RequiresDependencies envia state.from, mas os destinos não o consomem; preexistente.
> 0496 | - source_spec: archive/avulsas/2026-10/spec-padronizar-verde-receitas.md
> 0497 |   summary: Avaliar abertura direta dos formulários nos links Cadastrar Conta e Cadastrar Categoria.
> 0498 |   evidence: Os links abrem listagens e exigem outra ação para iniciar cadastro; preexistente.

## Resultado final da validação interativa — 21 origens decididas

Todas as decisões abaixo foram aprovadas por Guilherme nos grupos 1 a 6. Não houve implementação, teste de aplicação, medição, migração, criação de spec, épico ou história.

| Encaminhamento aprovado | IDs | Quantidade |
|---|---|---:|
| Planejar correções/verificações de segurança | DW-13, DW-14, DW-26, DW-35, DW-36, DW-37, DW-60, DW-176 | 8 |
| Planejar com pré-condição | DW-161 — conferir catálogo e preservar UNIQUE antes de remover índice redundante; vinculado a A13 | 1 |
| Investigar antes de decidir implementação | DW-2, DW-160, DW-117, DW-139, DW-147, DW-153, DW-155 | 7 |
| Consolidado em outro registro | DW-38 em DW-160 | 1 |
| Descartados deste planejamento | DW-63, DW-27, DW-28, DW-92 | 4 |
| **Total das origens iniciais** | | **21** |

- **A10 do sprint:** investigação DW-139 aprovada; manter open até resolver e verificar todo o escopo aprovado.
- **A13 do sprint:** investigação DW-155 e planejamento condicional DW-161 aprovados; concluir apenas a remoção do índice redundante não encerra a ação inteira.
- **Conclusão obrigatória de futuras histórias:** declarar os DW e action items que resolve; atualizar backlog/deferred-work com data e evidência após testes/revisão e antes de marcar a história done. Não encerrar registro parcialmente atendido. História de investigação só pode afirmar investigação concluída, nunca correção implementada; eventual mudança de escopo do registro precisa decisão explícita.
- **Próxima etapa:** gerar planejamento apenas a partir destas decisões quando solicitado; não tratar investigações como otimizações ou mudanças arquiteturais já aprovadas.
