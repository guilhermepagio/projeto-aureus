---
title: 'Story 1.3: Proteção de Sessão e Isolamento de Dados'
type: 'feature'
created: '2026-08-18'
status: 'done'
baseline_commit: '3c12734dc41dc70232b37879e381b15dd4eba315'
review_loop_iteration: 0
context: ['_bmad-output/implementation-artifacts/epic-1-context.md']
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O sistema necessita de garantia estrutural para que nenhum usuário acesse registros financeiros alheios. Adicionalmente, o cookie da sessão do JWT requer proteção SameSite para prevenir ataques CSRF com maior robustez.

**Approach:** Aperfeiçoar o cookie `AUREUS_SESSION` para utilizar a API `ResponseCookie` do Spring, configurando `SameSite=Lax`. Implementar suporte nativo ao isolamento de dados multilocatário (logical multi-tenancy) provido pelo Hibernate 6, usando `@TenantId` numa entidade base, associado a um contexto de thread alimentado pelo filtro JWT.

## Boundaries & Constraints

**Always:**
- Utilizar a anotação `@TenantId` do Hibernate em uma classe `@MappedSuperclass` (ex: `TenantAwareEntity`) para garantir isolamento por tenant no nível do banco.
- Abastecer o contexto de tenant a partir do filtro JWT (`JwtAuthenticationFilter`), garantindo que o ID de escopo venha estritamente de um token válido.
- Refatorar a emissão do cookie para usar `org.springframework.http.ResponseCookie` de forma a habilitar a flag `SameSite`.

**Ask First:**
- Utilizar bibliotecas de terceiros para gerenciar o Multi-Tenancy em vez da solução nativa do Hibernate 6.

**Never:**
- Permitir bypass do filtro `@TenantId` a não ser em consultas administrativas (que não existem no escopo atual).
- Aceitar identificadores de usuário (usuario_id) como parâmetro via URL ou Body para definir o escopo da operação.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Injeção do Tenant | JWT válido trafegado no Cookie | Filtro JWT popula o escopo do Hibernate com o ID do usuário local | N/A |
| Requisição não autenticada | Sem JWT válido | Rejeição no Spring Security antes de acessar persistência | 401 Unauthorized |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java` -- Onde o JWT é emitido; requer uso de `ResponseCookie`.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java` -- Onde o JWT é validado; aqui o ID do usuário (Tenant ID) deve ser fixado num contexto thread-local.

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java` -- Refatorar a adição de cookie para utilizar o cabeçalho `Set-Cookie` construído com `ResponseCookie.from("AUREUS_SESSION").httpOnly(true).sameSite("Lax").path("/").maxAge(86400).build()`.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/TenantContext.java` -- Criar classe com `ThreadLocal<String>` para armazenar e expor globalmente o ID (ou Google Subject ID) do usuário logado na thread corrente.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java` -- Criar componente Spring implementando `CurrentTenantIdentifierResolver` (do org.hibernate.context.spi) retornando o valor do `TenantContext` ou um tenant default se nulo.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java` -- Modificar para chamar `TenantContext.setTenantId(googleSubjectId)` logo após a validação bem-sucedida, e usar um bloco `finally` ao fim de `doFilterInternal` para invocar `TenantContext.clear()`.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java` -- Criar `@MappedSuperclass` com o campo `String tenantId` (ou `usuarioId`) anotado com `@org.hibernate.annotations.TenantId`.

**Acceptance Criteria:**
- Given a necessidade de segurança do Cookie, when inspecionamos a resposta de login, then o cabeçalho `Set-Cookie` apresenta `HttpOnly` e `SameSite=Lax`.
- Given o processamento de uma requisição autenticada, when um token válido é fornecido, then o filtro de segurança define corretamente o `TenantContext` durante o tempo de vida do request e o Hibernate consegue resolver o identificador dinamicamente via `CurrentTenantIdentifierResolver`.

## Suggested Review Order

**Isolamento de Tenant (Contexto da Thread)**

- Define a estrutura base isolada por tenant para as entidades JPA.
  [`TenantAwareEntity.java:8`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java#L8)

- Armazena o Tenant ID na thread corrente da requisição via `ThreadLocal`.
  [`TenantContext.java:6`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/TenantContext.java#L6)

- Resolve dinamicamente o tenant atual do `TenantContext` para o Hibernate.
  [`CurrentTenantIdentifierResolverImpl.java:10`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java#L10)

- Registra o bean do resolver customizado de tenant para autoconfiguração do Hibernate.
  [`application.yaml:14`](../../backend/src/main/resources/application.yaml#L14)

**Autenticação e SessãoSegura**

- Intercepta requisições e registra o `TenantContext` baseado no JWT válido.
  [`JwtAuthenticationFilter.java:54`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java#L54)

- Limpa o contexto do tenant da thread ao final da execução do request.
  [`JwtAuthenticationFilter.java:67`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java#L67)

- Remove e expira o cookie de sessão via `ResponseCookie` caso o token seja inválido.
  [`JwtAuthenticationFilter.java:60`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/JwtAuthenticationFilter.java#L60)

- Emite novo cookie com proteção `SameSite=Lax` após um login bem-sucedido.
  [`OAuth2LoginSuccessHandler.java:84`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/security/OAuth2LoginSuccessHandler.java#L84)
