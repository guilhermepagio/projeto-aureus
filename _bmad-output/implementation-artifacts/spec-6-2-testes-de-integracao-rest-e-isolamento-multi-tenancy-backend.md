---
title: 'Story 6.2: Testes de Integração REST e Isolamento Multi-Tenancy (Backend)'
type: 'feature'
created: '2026-10-05'
status: 'done'
baseline_commit: 'a77b4ffa02cc7358864a1cf4bd53ddd470e0877e'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-6-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O backend carece de uma suíte de testes de integração automatizada em banco real e da separação formal no Maven entre ciclos rápido e pesado, deixando vulneráveis o isolamento de segurança JWT, a proteção estrita multi-tenancy e a integridade relacional entre contas, categorias e movimentações.

**Approach:** Configurar o `maven-failsafe-plugin` e Testcontainers PostgreSQL no `pom.xml`, ajustar o ponto de entrada de segurança para retornar `401 Unauthorized` em `/api/**`, e criar suíte de testes de integração MockMvc (`*IT.java`) validando segurança REST, isolamento multi-tenant (respostas 404 sem vazamento), contratos de DTOs Records e proteção de integridade referencial (respostas 400 em deleção de vínculos ativos).

## Boundaries & Constraints

**Always:**
- Testes unitários (`*Test.java`) devem rodar exclusivamente na fase `test` via Surefire (`mvn test`) de forma isolada em milissegundos sem Docker.
- Testes de integração (`*IT.java`) devem rodar na fase `verify` via Failsafe (`mvn verify`) utilizando Testcontainers com PostgreSQL real e migrações Flyway ativas.
- Requisições não autenticadas ou com token JWT inválido/expirado em `/api/**` devem receber `401 Unauthorized`.
- O isolamento multi-tenant deve ser absoluto: operações de leitura, edição ou exclusão de recursos pertencentes a outro usuário devem retornar `404 Not Found`, sem vazar a existência do recurso.
- Tentativas de referenciar contas ou categorias de outro usuário no payload de movimentações devem retornar `403 Forbidden` (`AccessDeniedException`).
- Tentativas de exclusão de Conta ou Categoria com movimentações financeiras vinculadas devem ser barradas pelo banco com violação de FK e respondidas como `400 Bad Request` pelo `GlobalExceptionHandler`.

**Ask First:**
- Qualquer alteração nos endpoints REST existentes ou nas definições de schema das tabelas no Flyway.

**Never:**
- Não usar bancos em memória (H2/HSQLDB) nos testes de integração — a validação de PostgreSQL e Flyway exige Postgres real via Testcontainers.
- Não misturar testes de integração no ciclo `mvn test`.
- Não inserir comentários no código com menções a histórias, tarefas ou prompts de IA.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Requisição API sem token | `GET /api/contas` sem cookie `AUREUS_SESSION` | `401 Unauthorized` | Ponto de entrada de autenticação REST |
| Requisição API com token expirado | `GET /api/contas` com cookie JWT expirado | `401 Unauthorized` e cabeçalho `Set-Cookie` de limpeza | Ponto de entrada de autenticação REST |
| Consulta de recurso de outro tenant | Usuário B executa `GET /api/contas/{id_de_A}` | `404 Not Found` (recurso invisível) | `ResourceNotFoundException` via `GlobalExceptionHandler` |
| Atualização de recurso de outro tenant | Usuário B executa `PUT /api/contas/{id_de_A}` com payload válido | `404 Not Found` | `ResourceNotFoundException` via `GlobalExceptionHandler` |
| Exclusão de recurso de outro tenant | Usuário B executa `DELETE /api/contas/{id_de_A}` | `404 Not Found` | Retorno `notFound()` do Controller |
| Listagem isolada por tenant | Usuário B executa `GET /api/contas` e `GET /api/despesas-fixas` | Retorna apenas os registros criados por B, nenhum de A | Filtro `@TenantId` do Hibernate |
| Vínculo cruzado de tenant | Usuário B cria despesa referenciando conta pertencente a A | `403 Forbidden` | `AccessDeniedException` via `GlobalExceptionHandler` |
| Exclusão com vínculo ativo | Usuário A tenta excluir conta que possui despesa ativa vinculada | `400 Bad Request` com mensagem de vínculo ativo | `DataIntegrityViolationException` via `GlobalExceptionHandler` |
| Exclusão sem vínculo ativo | Usuário A exclui despesa vinculada e depois exclui a conta | `204 No Content` | Exclusão realizada com sucesso |
| Validação de payload DTO | Usuário A envia `POST /api/contas` com descrição em branco | `400 Bad Request` contendo `fieldErrors.descricao` | `MethodArgumentNotValidException` via `GlobalExceptionHandler` |

</frozen-after-approval>

## Code Map

- `backend-api/pom.xml` -- Configuração do `maven-failsafe-plugin` para execução de `**/*IT.java` na fase `verify` e inclusão das dependências `spring-boot-testcontainers`, `testcontainers-postgresql` e `testcontainers-junit-jupiter`
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java` -- Configuração explícita de `AuthenticationEntryPoint` para requisições `/api/**` garantindo resposta HTTP 401 para requisições não autenticadas
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java` -- Classe base abstrata para testes de integração gerenciando o container PostgreSQL singleton (Testcontainers), Flyway, MockMvc e utilitários de geração de cookies de autenticação
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationIT.java` -- Teste de integração de subida de contexto Spring Boot e migração Flyway em banco real
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java` -- Suíte de testes de integração para validação de segurança REST: rejeição 401 sem token, token inválido e token expirado
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java` -- Suíte de integração validando isolamento estrito multi-tenant (operações CRUD isoladas, respostas 404 sem vazamento e rejeição 403 em vínculos cruzados)
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java` -- Suíte de integração cobrindo validação de DTOs Records, integridade referencial com Postgres (400 Bad Request em deleção com FK ativa) e respostas do `@RestControllerAdvice`

## Tasks & Acceptance

**Execution:**
- [x] `backend-api/pom.xml` -- Adicionar dependências do Testcontainers e configurar `maven-failsafe-plugin` -- Habilitar ciclo `mvn verify` com execução de `*IT.java`
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java` -- Configurar `HttpStatusEntryPoint(UNAUTHORIZED)` para `/api/**` -- Garantir retorno de status 401 em vez de redirecionamento OAuth2 para APIs REST
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java` -- Criar classe base para testes de integração com Testcontainers PostgreSQL singleton e helpers de MockMvc/JWT -- Centralizar infraestrutura de banco real efêmero
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationIT.java` -- Atualizar teste para herdar de `AbstractIntegrationTest` -- Assegurar contexto Spring e migrações Flyway verdes no banco real
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java` -- Implementar testes de integração de segurança REST -- Validar cenários de requisição anônima, token malformado e token expirado
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java` -- Implementar testes de integração de multi-tenancy -- Validar isolamento de dados entre múltiplos usuários (404 em acessos indevidos e 403 em vínculos cruzados)
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java` -- Implementar testes de contratos REST e integridade relacional -- Validar rejeição 400 em exclusão de conta/categoria vinculada e validações de DTO

**Acceptance Criteria:**
- Given a suíte de testes do backend, when executado `mvn test`, then apenas os testes unitários (`*Test.java`) devem ser executados em milissegundos sem carregar Testcontainers.
- Given a suíte de testes do backend, when executado `mvn verify`, then o `maven-failsafe-plugin` deve executar todos os testes de integração (`*IT.java`) utilizando Testcontainers PostgreSQL real.
- Given uma requisição para qualquer endpoint de `/api/**`, when não houver cookie `AUREUS_SESSION` ou o token estiver expirado/inválido, then a resposta deve ser `401 Unauthorized`.
- Given recursos financeiros pertencentes ao Usuário A, when o Usuário B tentar acessá-los, editá-los ou excluí-los via API REST, then a resposta deve ser `404 Not Found`.
- Given o Usuário B tentando criar uma movimentação referenciando uma Conta ou Categoria do Usuário A, when a requisição for processada, then a resposta deve ser `403 Forbidden`.
- Given uma Conta ou Categoria vinculada a movimentações financeiras ativas, when houver tentativa de exclusão, then o banco de dados deve barrar a exclusão e a API deve retornar `400 Bad Request` com resposta estruturada de integridade relacional.

## Spec Change Log
- 2026-10-05: Implementação concluída com 18 testes de integração via Testcontainers e separação estrita surefire/failsafe.

## Design Notes

Classe base de integração com container PostgreSQL singleton e injeção dinâmica de propriedades:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

Configuração de AuthenticationEntryPoint no `SecurityConfig`:
```java
http.exceptionHandling(exceptions -> exceptions
    .defaultAuthenticationEntryPointFor(
        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
        new AntPathRequestMatcher("/api/**")
    )
);
```

## Verification

**Commands:**
- `./mvnw test` -- expected: `BUILD SUCCESS` executando apenas testes unitários em milissegundos com 0 falhas
- `./mvnw verify` -- expected: `BUILD SUCCESS` executando testes unitários e testes de integração (`*IT.java`) com Testcontainers PostgreSQL ativo com 0 falhas

## Suggested Review Order

**Configuração Maven e Segurança REST**

- Configuração do maven-failsafe-plugin e dependências do Testcontainers no pom.xml
  [`pom.xml:124`](../../backend-api/pom.xml#L124)

- Configuração do ponto de entrada para retornar 401 em rotas /api/**
  [`SecurityConfig.java:38`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java#L38)

- Implementação de busca por ID com lançamento de ResourceNotFoundException
  [`ContaController.java:37`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java#L37)

**Infraestrutura de Testes de Integração**

- Classe base abstrata gerenciando Testcontainers PostgreSQL singleton, propriedades dinâmicas e helpers JWT
  [`AbstractIntegrationTest.java:24`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/AbstractIntegrationTest.java#L24)

- Validação de subida de contexto Spring Boot e migrações Flyway no banco real
  [`BackendApplicationIT.java:16`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationIT.java#L16)

**Suíte de Integração e Verificação**

- Testes de segurança REST cobrindo anônimo, token expirado, token inválido e CSRF
  [`SecurityIT.java:17`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/security/SecurityIT.java#L17)

- Testes de isolamento estrito multi-tenant em listagens, consultas, edições, exclusões e vínculos cruzados
  [`MultiTenancyIT.java:33`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/MultiTenancyIT.java#L33)

- Testes de integridade referencial com Postgres e validações de DTOs Records
  [`RestContractAndIntegrityIT.java:29`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/integration/RestContractAndIntegrityIT.java#L29)
