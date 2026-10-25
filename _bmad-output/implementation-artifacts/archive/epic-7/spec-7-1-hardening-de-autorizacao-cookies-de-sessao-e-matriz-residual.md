---
title: 'Story 7.1: Hardening de Autorização, Cookies de Sessão e Matriz Residual CSRF/Multi-Tenancy (Backend)'
type: 'feature'
created: '2026-10-19'
status: 'done'
baseline_commit: '6a83233024003de2d7823f927d92a9a1c071b432'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-7-context.md'
  - '_bmad-output/specs/spec-epico-7/SPEC.md'
  - '_bmad-output/specs/spec-epico-7/seguranca.md'
  - '_bmad-output/specs/spec-epico-7/rastreabilidade.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O `SecurityConfig.java` encerra a cadeia de autorização com `anyRequest().permitAll()` sem política explícita para o Actuator (`DW-13`), e a suíte de integração carece de verificações completas para os atributos de segurança/TTL do cookie `AUREUS_SESSION` no login OAuth e logout (`DW-14`, `DW-36`), bem como para `PUT`/`DELETE` sem CSRF e vínculos cruzados residuais de conta/categoria entre tenants (`DW-176`).

**Approach:** Definir a allowlist pública explícita (`/api/auth/me`, `/api/auth/logout`, `/api/oauth2/authorization/**`, `/login/oauth2/code/**`, `/actuator/health`, `/error`), restringir a exposição do Actuator no `application.yaml` apenas a `health` sem detalhes sensíveis e substituir o fallback aberto por `.anyRequest().denyAll()` com `HttpStatusEntryPoint(UNAUTHORIZED)` padrão. Completar os testes automatizados em `SecurityIT` e `MultiTenancyIT` cobrindo todos os atributos do cookie `AUREUS_SESSION`, rejeição `403` sem mutação em `PUT`/`DELETE` sem CSRF e toda a matriz residual de vínculos cruzados (`403`) e recursos alheios (`404`).

## Boundaries & Constraints

**Always:**
- **Escopo rastreado:** `CAP-1` (`DW-13`), `CAP-2` (`DW-14`, `DW-36`) e `CAP-5` (`DW-176`) | **Action items:** `nenhum`.
- Preservar o fluxo OAuth2 legítimo (`/api/oauth2/authorization/**` e callback `/login/oauth2/code/**`), `GET /api/auth/me` retornando `401` para anônimo e `200` para autenticado, `POST /api/auth/logout` exigindo CSRF, e `GET /actuator/health` público restrito ao status básico (`show-details: never`).
- Rejeitar rotas fora da allowlist e endpoints operacionais não expostos (`/actuator/env`, `/actuator/beans`, rotas arbitrárias) sem permitir acesso anônimo ou autenticado indevido.
- Verificar no cookie `AUREUS_SESSION` todos os atributos contratuais: `HttpOnly`, `Path=/`, `SameSite=Lax`, `Secure` condicionado a `request.isSecure()` (HTTP vs HTTPS), `Max-Age=86400` (`jwt.expiration / 1000`) na emissão OAuth e valor vazio com `Max-Age=0` no logout e na limpeza por token expirado/inválido.
- Em `PUT` e `DELETE` autenticados sem token CSRF e nas tentativas de vínculo cruzado (`POST`/`PUT`), comprovar tanto o código HTTP (`403 Forbidden` ou `404 Not Found`) quanto a ausência de mutação ou persistência indevida no banco.
- Atualizar `DW-13`, `DW-14`, `DW-36` e `DW-176` em `_bmad-output/implementation-artifacts/deferred-work.md` com data ISO, evidência de testes, revisão, pendências e estado antes de marcar a história como `done`.

**Ask First:**
- Qualquer alteração na política pública proposta para Q-1 (exposição além de `/actuator/health` ou inclusão de novas rotas públicas fora do fluxo OAuth/Auth).

**Never:**
- Não aplicar `denyAll()` antes das regras da allowlist pública e de `/api/**` de modo a bloquear o início ou o callback do OAuth2.
- Não incluir testes de imagem/avatar de perfil (`DW-14`/`DW-36`) nem expandir validações genéricas/exaustivas de DTOs (`DW-176`).
- Não inserir comentários no código-fonte com referências a épicos, stories ou IDs de backlog (`DW-*`).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Início OAuth público (`SEG-01..03`) | `GET /api/oauth2/authorization/google` anônimo | `302 Found` redirecionando ao provedor OAuth | Allowlist pública explícita |
| Callback OAuth sem credencial válida (`SEG-01..03`) | `GET /login/oauth2/code/google` anônimo sem código válido | `302 Found` para `/login?error=true` via `OAuth2LoginFailureHandler` (não bloqueado pelo fallback) | Fluxo OAuth preservado |
| Healthcheck público e demais endpoints Actuator bloqueados (`SEG-01..02`) | `GET /actuator/health` vs `GET /actuator/env` (anônimo ou autenticado) | `/actuator/health` retorna `200 OK`; `/actuator/env` e rotas desconhecidas retornam `401` (anônimo) ou `403` (autenticado) | Exposição restrita a `health` + `.anyRequest().denyAll()` |
| Emissão de cookie no sucesso OAuth (`SEG-04` / `DW-36`) | `OAuth2LoginSuccessHandler` em requisição HTTP (`secure=false`) e HTTPS (`secure=true`) | `Set-Cookie: AUREUS_SESSION=<jwt>` com `HttpOnly`, `Path=/`, `SameSite=Lax`, `Max-Age=86400`, `Secure` apenas em HTTPS | N/A |
| Expiração de cookie no logout (`SEG-05` / `DW-14`) | `POST /api/auth/logout` com CSRF em HTTP e HTTPS; e sem CSRF | Com CSRF: `200 OK`, `AUREUS_SESSION=""`, `Max-Age=0`, `Path=/`, `HttpOnly`, `SameSite=Lax`, `Secure` conforme transporte; sem CSRF: `403 Forbidden` | Rejeição CSRF do Spring Security |
| `PUT` e `DELETE` autenticados sem CSRF (`SEG-15` / `DW-176`) | `PUT /api/contas/{id}` e `DELETE /api/contas/{id}` com cookie válido mas sem token CSRF | `403 Forbidden` e registro original permanece intacto no banco | Filtro CSRF barra mutação |
| Vínculos cruzados residuais em criação (`POST`) (`SEG-16` / `DW-176`) | Tenant B cria `DespesaVariavel` (Cat de A), `ReceitaFixa` (Cat de A) ou `ReceitaVariavel` (Conta de A) | `403 Forbidden` e nenhuma movimentação é persistida para B | `AccessDeniedException` no Service |
| Vínculos cruzados residuais em atualização (`PUT`) (`SEG-16` / `DW-176`) | Tenant B atualiza `DespesaFixa` (Cat de A), `DespesaVariavel` (Conta ou Cat de A), `ReceitaFixa` (Conta ou Cat de A) ou `ReceitaVariavel` (Conta ou Cat de A) | `403 Forbidden` e movimentação original de B mantém conta/categoria e valores anteriores | `AccessDeniedException` no Service |
| Acesso/edição/exclusão cross-tenant em movimentações (`SEG-16` / `DW-176`) | Tenant B executa `GET`, `PUT` ou `DELETE` em `DespesaVariavel`, `ReceitaFixa` ou `ReceitaVariavel` pertencente a A | `404 Not Found` e movimentação de A permanece inalterada | Filtro `@TenantId` oculta registro alheio |

</frozen-after-approval>

## Code Map

- `backend-api/src/main/resources/application.yaml` -- Configurar exposição web do Actuator restrita exclusivamente a `health` (`management.endpoints.web.exposure.include: health` e `management.endpoint.health.show-details: never`, linhas 39–47).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java` -- Configurar `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` (linhas 37–39), allowlist pública explícita (`/api/auth/me`, `/api/auth/logout`, `/api/oauth2/authorization/**`, `/login/oauth2/code/**`, `/actuator/health`, `/error`), `/api/**` autenticado e fallback `.anyRequest().denyAll()` (linhas 40–51).
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java` -- Referência somente leitura (linhas 61–68) para emissão do cookie `AUREUS_SESSION`.
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/AuthController.java` -- Referência somente leitura (linhas 29–55) para `/api/auth/me` e `/api/auth/logout`.
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java` -- Expandir suíte de integração para validar política pública/Actuator/fallback (`CAP-1`), atributos completos do cookie `AUREUS_SESSION` em OAuth/logout/token inválido (`CAP-2`) e rejeição `403` sem mutação em `PUT`/`DELETE` sem CSRF (`CAP-5`).
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java` -- Completar matriz residual de vínculos cruzados (`POST` e `PUT` com conta/categoria de outro tenant retornando `403` sem persistir alteração) e operações cross-tenant (`GET` de listagem ocultando registros alheios e `PUT`/`DELETE` retornando `404` sem mutação) para `DespesaFixa`, `DespesaVariavel`, `ReceitaFixa` e `ReceitaVariavel` (`CAP-5`).
- `_bmad-output/implementation-artifacts/deferred-work.md` -- Atualizar registros `DW-13`, `DW-14`, `DW-36` e `DW-176` com data ISO, evidência de testes/revisão e status após verificação.

## Tasks & Acceptance

**Execution:**
- [x] `backend-api/src/main/resources/application.yaml` -- Restringir exposição do Actuator a `health` com `show-details: never` -- Garantir que endpoints operacionais sensíveis não fiquem expostos por configuração padrão ou futura (`SEG-01`)
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java` -- Definir allowlist pública explícita (`/api/auth/me`, `/api/auth/logout`, `/api/oauth2/authorization/**`, `/login/oauth2/code/**`, `/actuator/health`, `/error`), proteger `/api/**` com `authenticated()`, aplicar `.anyRequest().denyAll()` e configurar `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` como entry point de exceções -- Eliminar o fallback permissivo preservando OAuth2 e contratos HTTP (`SEG-01` a `SEG-03`)
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java` -- Adicionar testes para: (1) allowlist OAuth/callback, `/actuator/health` público e bloqueio de `/actuator/env` e rotas não mapeadas; (2) todos os atributos do cookie `AUREUS_SESSION` no `OAuth2LoginSuccessHandler` e no `/api/auth/logout` em HTTP e HTTPS (`HttpOnly`, `Path=/`, `SameSite=Lax`, `Max-Age`, `Secure`) e rejeição de logout sem CSRF; (3) `PUT` e `DELETE` autenticados sem CSRF retornando `403` sem alterar/excluir dados -- Comprovar `CAP-1`, `CAP-2` e `SEG-15`
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java` -- Completar os casos residuais da matriz cross-tenant: `POST` com categoria alheia em `DespesaVariavel` e `ReceitaFixa` e conta alheia em `ReceitaVariavel` (`403` sem criar registro); `PUT` com conta/categoria alheia nos casos faltantes das 4 entidades de movimentação (`403` mantendo estado original); e `GET`/`PUT`/`DELETE` em recurso de outro tenant para `DespesaVariavel`, `ReceitaFixa` e `ReceitaVariavel` (`404` sem mutação) -- Comprovar `SEG-14` e `SEG-16`
- [x] `_bmad-output/implementation-artifacts/deferred-work.md` -- Registrar evidência executada, revisão, data ISO e encerramento de `DW-13`, `DW-14`, `DW-36` e `DW-176` conforme protocolo de `rastreabilidade.md` -- Cumprir regra obrigatória antes de marcar a história como `done`

**Acceptance Criteria:**
- Given a configuração de segurança em `SecurityConfig.java` e `application.yaml`, when requisições anônimas ou autenticadas acessarem rotas fora da allowlist pública e de `/api/**` (como `/actuator/env` ou `/rota-inexistente`), then o acesso deve ser negado (`401` para anônimo, `403` para autenticado), enquanto `/api/oauth2/authorization/google`, `/login/oauth2/code/google`, `/api/auth/me`, `/api/auth/logout` e `/actuator/health` mantêm seus contratos (`DW-13`).
- Given o login OAuth bem-sucedido via `OAuth2LoginSuccessHandler` e o encerramento via `POST /api/auth/logout`, when inspecionado o cabeçalho `Set-Cookie` em requisições HTTP e HTTPS, then `AUREUS_SESSION` deve conter `HttpOnly`, `Path=/`, `SameSite=Lax`, `Secure` apenas quando `request.isSecure()` for verdadeiro, `Max-Age=86400` no login e valor vazio com `Max-Age=0` no logout (`DW-14`, `DW-36`).
- Given um usuário autenticado com recursos existentes, when enviar `PUT` ou `DELETE` sem token CSRF, then a API deve responder `403 Forbidden` e o registro deve permanecer inalterado no banco de dados (`DW-176` / `SEG-15`).
- Given dois tenants isolados A e B, when B tentar criar (`POST`) ou atualizar (`PUT`) qualquer uma das 4 entidades de movimentação referenciando Conta ou Categoria de A, ou acessar/editar/excluir movimentação de A, then a API deve responder `403 Forbidden` para vínculo alheio e `404 Not Found` para recurso alheio, sem persistir mutação indevida (`DW-176` / `SEG-16`).

### Review Findings

- [x] [Review][Patch] Restaurar verificação de `POST /api/auth/logout` anônimo com CSRF válido para comprovar presença de `/api/auth/logout` na allowlist pública [`backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:222`]
- [x] [Review][Patch] Verificar diretamente via `WebEndpointsSupplier` que `management.endpoints.web.exposure.include` expõe exclusivamente `health`, sem depender apenas do bloqueio prévio do `SecurityFilterChain` [`backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:270`]
- [x] [Review][Patch] Exercitar a rota `/error` na suíte da allowlist pública para garantir que não caia em `.anyRequest().denyAll()` [`backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:292`]
- [x] [Review][Patch] Validar explicitamente `expectedMaxAgeSeconds == 86400` e remover asserção duplicada `assertThat(jwtHttps).isNotBlank()` no teste do `OAuth2LoginSuccessHandler` [`backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java:359`]
- [x] [Review][Patch] Sincronizar referências de linha de `SecurityIT.java`/`MultiTenancyIT.java` e registro da revisão de 4 camadas em `spec-7-1` e `deferred-work.md` [`_bmad-output/implementation-artifacts/deferred-work.md:45`]

## Spec Change Log

## Design Notes

**Matriz de Aceite `CAP-5` (`DW-176` — `SEG-14` a `SEG-16`):**

| Método | Recurso / Vínculo | Usuários Envolvidos | Estado Anterior | Resultado Esperado | Evidência (`SecurityIT` / `MultiTenancyIT`) |
|---|---|---|---|---|---|
| `POST` | `/api/contas` sem CSRF | 1 tenant autenticado | Existente (enriquecido) | `403 Forbidden` + lista vazia | `SecurityIT#requisicaoMutanteSemCsrfDeveRetornar403` |
| `PUT` / `DELETE` | `/api/contas/{id}` sem CSRF | 1 tenant autenticado | Lacuna preenchida | `403 Forbidden` + registro intacto | `SecurityIT#requisicoesPutEDeleteSemCsrfDevemRetornar403EManterRegistroIntacto` |
| `POST` | `DespesaFixa` com Conta ou Cat de A | Tenant A (dono) e B (autor) | Existente (enriquecido) | `403 Forbidden` + nenhum registro em A ou B | `MultiTenancyIT#deveRetornar403AoCriarDespesaFixaComContaDeOutroTenant` / `...CategoriaDeOutroTenant` |
| `POST` | `DespesaVariavel` com Conta ou Cat de A | Tenant A (dono) e B (autor) | Conta existente; Cat lacuna preenchida | `403 Forbidden` + nenhum registro em A ou B | `MultiTenancyIT#deveRetornar403AoCriarDespesaVariavelComContaDeOutroTenant` / `...CategoriaDeOutroTenant` |
| `POST` | `ReceitaFixa` com Conta ou Cat de A | Tenant A (dono) e B (autor) | Conta existente; Cat lacuna preenchida | `403 Forbidden` + nenhum registro em A ou B | `MultiTenancyIT#deveRetornar403AoCriarReceitaFixaComContaDeOutroTenant` / `...CategoriaDeOutroTenant` |
| `POST` | `ReceitaVariavel` com Conta ou Cat de A | Tenant A (dono) e B (autor) | Cat existente; Conta lacuna preenchida | `403 Forbidden` + nenhum registro em A ou B | `MultiTenancyIT#deveRetornar403AoCriarReceitaVariavelComContaDeOutroTenant` / `...CategoriaDeOutroTenant` |
| `PUT` | `DespesaFixa` de B com Conta ou Cat de A | Tenant A (dono vínculo) e B (autor) | Conta existente; Cat lacuna preenchida | `403 Forbidden` + estado de B intacto | `MultiTenancyIT#deveRetornar403AoAtualizarDespesaFixaComContaDeOutroTenant` / `...CategoriaDeOutroTenant` |
| `PUT` | `DespesaVariavel`, `ReceitaFixa`, `ReceitaVariavel` de B com Conta ou Cat de A | Tenant A (dono vínculo) e B (autor) | Lacuna preenchida | `403 Forbidden` + estado de B intacto | `MultiTenancyIT#deveRetornar403AoAtualizar{DespesaVariavel,ReceitaFixa,ReceitaVariavel}ComContaOuCategoriaDeOutroTenant` |
| `GET` / `PUT` / `DELETE` | `Conta` (`GET /{id}`, `PUT /{id}`, `DELETE /{id}`) e `Categoria` / 4 movimentações (`GET` listagem filtrada por `@TenantId`, `PUT /{id}`, `DELETE /{id}`) de A | Tenant A (dono) e B (autor) | `Conta`, `Categoria`, `DespesaFixa` existentes; demais lacunas preenchidas | `GET /{id}` `404` (`Conta`) ou `GET` lista `200 []`; `PUT`/`DELETE /{id}` `404 Not Found` + registro de A intacto | `MultiTenancyIT#deveRetornar404AoConsultarAtualizarOuExcluir{DespesaVariavel,ReceitaFixa,ReceitaVariavel}DeOutroTenant` |

## Verification

**Commands:**
- `./mvnw test` -- expected: `BUILD SUCCESS` para todos os testes unitários
- `./mvnw verify -Pintegration-test` -- expected: `BUILD SUCCESS` para `SecurityIT`, `MultiTenancyIT`, `RestContractAndIntegrityIT` e `BackendApplicationIT`

## Suggested Review Order

**Hardening de autorização e política do Actuator (`CAP-1` / `DW-13`)**

- Define allowlist pública explícita, fallback `.anyRequest().denyAll()` e `HttpStatusEntryPoint(UNAUTHORIZED)`.
  [`SecurityConfig.java:37`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java#L37)

- Restringe exposição web do Actuator somente a `health` com `show-details: never`.
  [`application.yaml:39`](../../backend-api/src/main/resources/application.yaml#L39)

- Verifica allowlist OAuth, `/actuator/health` sem componentes, `/error` permitido, `WebEndpointsSupplier` e bloqueio de rotas não autorizadas.
  [`SecurityIT.java:269`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java#L269)

**Atributos de segurança e expiração do cookie `AUREUS_SESSION` (`CAP-2` / `DW-14`, `DW-36`)**

- Valida emissão OAuth (`HttpOnly`, `Path=/`, `SameSite=Lax`, `Max-Age=86400`, `Secure` por transporte e subject JWT).
  [`SecurityIT.java:345`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java#L345)

- Valida expiração de cookie no `/api/auth/logout` (anônimo e autenticado) em HTTP/HTTPS e rejeição sem CSRF.
  [`SecurityIT.java:209`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java#L209)

- Valida atributos completos do cookie de limpeza em respostas `401` por token expirado/inválido.
  [`SecurityIT.java:78`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java#L78)

**Matriz residual CSRF e isolamento multi-tenant (`CAP-5` / `DW-176`)**

- Comprova `403 Forbidden` e preservação integral do registro em `PUT` e `DELETE` sem CSRF.
  [`SecurityIT.java:167`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java#L167)

- Completa casos de `POST` e `PUT` com conta/categoria de outro tenant (`403` sem mutação).
  [`MultiTenancyIT.java:207`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java#L207)

- Completa matriz `GET` listagem e `PUT`/`DELETE /{id}` (`404` sem mutação) nas 4 movimentações.
  [`MultiTenancyIT.java:814`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java#L814)

**Rastreabilidade e fechamento do backlog (`rastreabilidade.md`)**

- Registra data ISO, evidência de testes/revisão e encerramento de `DW-13`, `DW-14`, `DW-36` e `DW-176`.
  [`deferred-work.md:32`](./deferred-work.md#L32)

