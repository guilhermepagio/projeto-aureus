# Epic 7 Context: Segurança, Desempenho e Observabilidade da Aplicação

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Consolidar as garantias operacionais de segurança, desempenho e observabilidade do Aureus: restringir o fallback de autorização à allowlist pública explícita, validar atributos de cookies de sessão e a matriz residual de CSRF e vínculos cruzados entre tenants, instrumentar correlação por requisição (`MDC`) e logs estruturados sem vazamento de dados sensíveis, limpar o estado privado do frontend ao detectar `401` em uso ou falha de rede no logout, garantir índices de `usuario_id` em todas as tabelas filtradas por `TenantContext` via migração incremental Flyway V2 e produzir evidências reproduzíveis para as investigações técnicas de segurança e banco de dados.

## Stories

- Story 7.1: Hardening de Autorização, Cookies de Sessão e Matriz Residual CSRF/Multi-Tenancy (Backend)
- Story 7.2: Isolamento de Contexto, Correlação MDC e Logs Estruturados de Troubleshooting (Backend)
- Story 7.3: Tratamento de Sessão Inválida (401 em Uso) e Limpeza de Estado Privado no Logout (Frontend)
- Story 7.4: Migração Flyway V2 (Índices de Tenant Obrigatórios e Redundâncias) e Avaliação de Índices/FKs
- Story 7.5: Investigações Técnicas de Segurança (Revogação JWT e Ausência de Tenant) e Desempenho (Consolidação, N+1 e Listagens)

## Requirements & Constraints

- **Política pública e autorização (CAP-1 / DW-13):** Inventariar rotas públicas necessárias (início/callback OAuth e política explícita para endpoints do Actuator) e substituir o fallback aberto `anyRequest().permitAll()` em `SecurityConfig.java`. Preservar OAuth legítimo, `401` em `/api/auth/me` anônimo e contrato de logout com CSRF.
- **Cookies de sessão (CAP-2 / DW-14, DW-36):** Comprovar por testes automatizados os atributos de `AUREUS_SESSION` na emissão OAuth (`HttpOnly`, `Path=/`, `SameSite=Lax`, `Secure` conforme transporte, `Max-Age` coerente com `jwt.expiration / 1000`) e na expiração do logout (valor vazio, `Max-Age=0` e mesmas flags).
- **Sessão inválida e limpeza local no frontend (CAP-3 / DW-26, DW-60):** Garantir limpeza de `authStore` e cache privado do `QueryClient` com retorno ao login em logout (com sucesso ou falha de rede) e ao receber `401 Unauthorized` durante o uso, preservando contratos de erro `403`, `404` e `400`. Distinguir limpeza local no cliente de invalidação no servidor.
- **Contextos de execução e resolver multi-tenant (CAP-4 / DW-35, DW-37, DW-160):** Verificar população e limpeza de `TenantContext` e `SecurityContextHolder` (inclusive em erro/exceção e reuso de thread) e o contrato atual do resolver de tenant (`public` para nulo/vazio/espaços) sem alterar o fallback antes da investigação de bootstrap e acesso sem contexto.
- **Matriz residual de CSRF e vínculos cruzados (CAP-5 / DW-176):** Inventariar a cobertura existente e completar apenas os testes residuais faltantes para `PUT`/`DELETE` autenticados sem token CSRF (`403` sem mutação) e vínculos cruzados de conta/categoria entre tenants (`403` para vínculo alheio e `404` para recurso não acessível).
- **Observabilidade e correlação por requisição (CAP-10 / OBS-01..05):** Usar SLF4J/Logback nativo sem stack externa. Preencher `MDC` com `requestId` e `tenantId` (ou marcador anônimo) e limpar em `finally`. Registrar logs padronizados em autenticação, `GlobalExceptionHandler` e serviços financeiros proibindo estritamente logar JWT, cookies (`AUREUS_SESSION`, `XSRF-TOKEN`), valores monetários, descrições financeiras privadas ou dados pessoais de perfil.
- **Índices e migração Flyway V2 (CAP-8 / DW-155, DW-161 / A13):** Manter `V1__create_performance_indexes.sql` imutável. Criar obrigatoriamente na `V2` `idx_contas_usuario_id` e `idx_categorias_usuario_id` (sem duplicar índice simples em `usuario_id` nas 4 tabelas de movimentações). Avaliar índices de FKs e índices de data sem tenant pelo catálogo/planos e remover índice secundário em `usuarios.google_subject_id` somente se comprovada redundância preservando `UNIQUE`.
- **Investigações técnicas de segurança e desempenho (CAP-6, CAP-7, CAP-9 / DW-2, DW-139, DW-117, DW-147, DW-153 / A10):** Produzir medições e relatórios reproduzíveis antes de decidir otimizações ou mudanças arquiteturais (sem implementar renovação automática, tabela de sessões, paginação, JOIN FETCH ou SQL agregado antecipadamente).
- **Protocolo de rastreabilidade obrigatório:** Nenhuma história pode ser marcada como `done` sem atualizar previamente seus registros `DW-*` em `_bmad-output/implementation-artifacts/deferred-work.md` e action items (`A10`/`A13`) em `_bmad-output/implementation-artifacts/sprint-status.yaml` com data ISO, evidência de testes/medições, revisão, pendências e estado.

## Technical Decisions

- **Imutabilidade de migrações:** Qualquer evolução de banco ocorre exclusivamente em `V2+` sob `backend-api/src/main/resources/db/migration/`.
- **Contratos HTTP:** Preservar `401` para não autenticado, `403` para falha de CSRF ou vínculo de conta/categoria pertencente a outro tenant, e `404` para recurso principal inexistente ou pertencente a outro tenant.
- **Observabilidade nativa:** Configurar console pattern no `application.yaml` com chaves do `MDC` (`requestId`, `tenantId`), sem introduzir dependências externas de telemetria.

## Cross-Story Dependencies

- **Story 7.2 e Story 7.5 (CAP-4):** Story 7.2 verifica o contrato vigente do `CurrentTenantIdentifierResolverImpl` e a limpeza de contextos (`DW-35`, `DW-37`) sem alterar o fallback `public`, que depende da investigação de bootstrap em Story 7.5 (`DW-160`).
- **Story 7.4 e Story 7.5 (CAP-8 / CAP-9):** A migração Flyway V2 em Story 7.4 entrega os índices obrigatórios e avalia FKs/redundâncias no catálogo (`DW-155`, `DW-161`), enquanto Story 7.5 mede os custos de consolidação e listagens/DTOs (`DW-139`, `DW-117`, `DW-147`, `DW-153`).
