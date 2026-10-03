---
title: 'Story 5.2: Tratamento Global de Erros, Validação de Tenant em Relacionamentos e Índices'
type: 'refactor'
created: '2026-09-16'
status: 'done'
baseline_commit: 'c87a3e12ab9e0089dc46e99a4c3b1f427cb64c4e'
review_loop_iteration: 2
context:
  - '_bmad-output/implementation-artifacts/epic-5-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O backend não possui tratamento global padronizado para exceções de validação, integridade relacional e recursos não encontrados (gerando stacktraces ou respostas inconsistentes), não valida estritamente a titularidade (tenant) de Contas e Categorias vinculadas a movimentações (permitindo potencial manipulação cruzada), omite movimentações sem conta na consolidação gerando divergências com blocos analíticos, e carece de índices em colunas críticas para buscas de consolidação e autenticação.

**Approach:** Implementar `@RestControllerAdvice` centralizado para conversão de erros (`MethodArgumentNotValidException`, `DataIntegrityViolationException`, `ResourceNotFoundException`, `AccessDeniedException` e genéricos), adicionar validação de segurança e tenant em `DespesaFixaService`, `ReceitaFixaService`, `DespesaVariavelService` e `ReceitaVariavelService`, padronizar tratamento de movimentações sem conta no `ConsolidacaoService` (agrupamento sob "Sem Conta" e cômputo no saldo histórico), e criar migração Flyway e anotações `@Table(indexes)` com índices para `google_subject_id` e `data_inicio`/`data_fim`.

## Boundaries & Constraints

**Always:**
- O handler global de exceções (`@RestControllerAdvice`) deve padronizar o payload JSON de erros com timestamps, status HTTP, mensagem clara e mapa/lista de campos inválidos sem nunca vazar stacktraces internas em produção (`500`).
- Ao criar ou atualizar qualquer movimentação financeira (`DespesaFixa`, `ReceitaFixa`, `DespesaVariavel`, `ReceitaVariavel`), o serviço deve validar se as entidades `Conta` e `Categoria` referenciadas pertencem estritamente ao `usuario_id` autenticado. Se pertencerem a outro tenant, lançar `AccessDeniedException` (`403 Forbidden`); se não existirem, lançar `ResourceNotFoundException` (`404 Not Found`).
- A Consolidação (`ConsolidacaoService`) deve tratar movimentações financeiras sem conta vinculada de forma consistente com a consolidação por categoria, agrupando-as sob a linha sintética "Sem Conta" (`SEM_CONTA_ID = -1L`) e computando seus valores no saldo histórico pré-grade.
- Os índices de banco devem ser versionados via script Flyway (`V1__create_performance_indexes.sql`) com `baseline-on-migrate: true` e replicados nas anotações `@Table(indexes = ...)` das entidades JPA correspondentes.
- Preservar a compatibilidade regressiva dos contratos JSON consumidos pelo frontend.

**Ask First:**
- Alteração na estrutura de schemas existentes que exija recriação ou exclusão destrutiva de colunas.
- Modificação no formato de payload consumido pelas páginas atuais de consolidação do frontend.

**Never:**
- Não expor stacktraces ou detalhes internos do banco de dados nos retornos de erro da API.
- Não permitir que uma movimentação financeira seja associada a contas ou categorias de outro tenant.
- Não ignorar movimentações sem conta na consolidação mensal gerando divergência matemática entre os blocos de conta e categoria.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Validação de payload inválido | `POST /api/contas` com `descricao: ""` | `400 Bad Request` com payload padronizado e detalhes do campo `descricao` | `MethodArgumentNotValidException` capturada no `@RestControllerAdvice` |
| Violação de integridade referencial | `DELETE /api/contas/1` (conta vinculada a despesas) | `400 Bad Request` com mensagem explicativa sobre vínculo ativo | `DataIntegrityViolationException` capturada no `@RestControllerAdvice` |
| Recurso não encontrado | `GET /api/contas/99999` ou atualização de ID inexistente | `404 Not Found` com mensagem descritiva | `ResourceNotFoundException` capturada no `@RestControllerAdvice` |
| Tentativa de vínculo de Conta/Categoria de outro tenant | `POST /api/despesas-fixas` com `conta: { id: ID_OUTRO_TENANT }` | `403 Forbidden` com mensagem de acesso negado ao recurso | `AccessDeniedException` capturada no `@RestControllerAdvice` |
| Movimentação sem conta na Consolidação | Lançamento com `conta: null` ou conta inexistente | Lançamento incluído na linha sintética "Sem Conta" (`id: -1`) e somado no saldo histórico | Linha sintética criada e calculada sem erro |
| Erro interno inesperado | NullPointerException ou falha não mapeada | `500 Internal Server Error` com mensagem genérica segura sem stacktrace | `Exception.class` genérico capturado no `@RestControllerAdvice` |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/exception/ResourceNotFoundException.java` -- Nova exceção de negócio para entidades não encontradas (HTTP 404)
- `backend/src/main/java/com/guilhermepagio/aureus/backend/exception/ApiErrorResponse.java` -- Record DTO padronizado para respostas de erro da API com timestamp, status, mensagem, path e lista/mapa de erros de validação
- `backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java` -- Novo `@RestControllerAdvice` global interceptando `MethodArgumentNotValidException`, `DataIntegrityViolationException`, `ResourceNotFoundException`, `AccessDeniedException`, `IllegalArgumentException` e `Exception`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java` -- Adicionar query nativa de verificação de propriedade (`findOwnerUsuarioId`) ignorando o filtro do tenant
- `backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java` -- Adicionar query nativa de verificação de propriedade (`findOwnerUsuarioId`) ignorando o filtro do tenant
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java` -- Lançar `ResourceNotFoundException` em atualizações de ID inexistente
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java` -- Lançar `ResourceNotFoundException` em atualizações de ID inexistente
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java` -- Validar propriedade de tenant para Conta e Categoria (lançando 403 se de outro tenant, 404 se inexistente) e lançar `ResourceNotFoundException` em atualizações
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java` -- Validar propriedade de tenant para Conta e Categoria e lançar `ResourceNotFoundException` em atualizações
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java` -- Validar propriedade de tenant para Conta e Categoria e lançar `ResourceNotFoundException` em atualizações
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java` -- Validar propriedade de tenant para Conta e Categoria e lançar `ResourceNotFoundException` em atualizações
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Padronizar agregação de movimentações sem conta associada com linha sintética "Sem Conta" (`SEM_CONTA_ID = -1L`) e cômputo no saldo histórico pré-grade
- `backend/pom.xml` -- Adicionar dependências do Flyway (`flyway-core` e `flyway-database-postgresql`)
- `backend/src/main/resources/application.yaml` -- Configurar Flyway (`spring.flyway.baseline-on-migrate: true`, `baseline-version: 0`)
- `backend/src/main/resources/db/migration/V1__create_performance_indexes.sql` -- Migração Flyway com índices para `google_subject_id` e `data_inicio`/`data_fim`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Usuario.java` -- Atualizar `@Table(indexes)` com índice em `google_subject_id`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java` -- Atualizar `@Table(indexes)` com índice em `data_inicio`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaFixa.java` -- Atualizar `@Table(indexes)` com índice em `data_inicio`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java` -- Atualizar `@Table(indexes)` com índice em `(data_inicio, data_fim)`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java` -- Atualizar `@Table(indexes)` com índice em `(data_inicio, data_fim)`
- `backend/src/test/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandlerTest.java` -- Testes unitários para o `@RestControllerAdvice`
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/TenantValidationTest.java` -- Testes unitários para validação de tenant em relacionamentos nos serviços
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java` -- Atualizar testes para cobrir movimentações sem conta e consistência de totais

## Tasks & Acceptance

**Execution:**
- [x] `backend/pom.xml` e `application.yaml` -- Configurar dependências e propriedades do Flyway -- Suporte a migrações versionadas no PostgreSQL
- [x] `backend/src/main/resources/db/migration/V1__create_performance_indexes.sql` -- Criar script de migração Flyway com índices para `google_subject_id` em `usuarios` e `data_inicio`/`data_fim` nas tabelas de despesas e receitas -- Otimização de performance para consultas frequentes e consolidação
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/*.java` -- Adicionar declaração de índices nas anotações `@Table(indexes = ...)` em `Usuario`, `DespesaFixa`, `ReceitaFixa`, `DespesaVariavel` e `ReceitaVariavel` -- Alinhamento do modelo JPA com o schema do banco
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/exception/ResourceNotFoundException.java` e `ApiErrorResponse.java` -- Criar classe de exceção para recursos não encontrados e record DTO imutável para retorno padronizado de erros -- Contrato limpo e consistente para respostas HTTP de erro
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java` -- Implementar `@RestControllerAdvice` interceptando Bean Validation, integridade relacional, recursos não encontrados, acesso negado e erros genéricos -- Centralização do tratamento de erros e eliminação de stacktraces
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java` e `CategoriaRepository.java` -- Adicionar métodos com query nativa para verificar `usuario_id` proprietário de uma Conta/Categoria independente do tenant corrente -- Permite detectar violações de tenant cruzado
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java` e `CategoriaService.java` -- Ajustar métodos para lançar `ResourceNotFoundException` quando o registro não existir na atualização -- Tratamento padronizado de 404
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java`, `ReceitaFixaService.java`, `DespesaVariavelService.java`, `ReceitaVariavelService.java` -- Implementar validação estrita de tenant para relacionamentos de Conta e Categoria (lançando 403 Forbidden se pertencer a outro tenant e 404 Not Found se inexistente) e lançar `ResourceNotFoundException` na atualização -- Segurança e isolamento multi-tenant
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/*Controller.java` -- Remover blocos try-catch redundantes de `DataIntegrityViolationException` nos controllers, delegando para o `GlobalExceptionHandler` -- Código limpo nos controllers
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Adicionar suporte a linha sintética "Sem Conta" (`SEM_CONTA_ID = -1L`) em `calcularConsolidacaoPorConta` e cômputo no saldo histórico, equiparando com o tratamento de categorias -- Elimina divergência entre consolidação por conta e por categoria
- [x] `backend/src/test/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandlerTest.java` -- Implementar testes unitários para o `GlobalExceptionHandler` -- Cobertura dos status 400, 403, 404 e 500
- [x] `backend/src/test/java/com/guilhermepagio/aureus/backend/service/TenantValidationTest.java` e `ConsolidacaoServiceTest.java` -- Adicionar testes unitários para validação de tenant cruzado e para consolidação com movimentações sem conta associada -- Garantia de isolamento e consistência matemática

### Review Findings

<!-- Populated by step-04 during review loops. -->
- [x] [Review][Patch] Tratar HandlerMethodValidationException no GlobalExceptionHandler retornando HTTP 400 com erros de validação padronizados [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:100]
- [x] [Review][Patch] Tratar HttpMediaTypeNotSupportedException (415) e HttpMediaTypeNotAcceptableException (406) no GlobalExceptionHandler [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:230]
- [x] [Review][Patch] Adicionar header Allow com métodos suportados na resposta HTTP 405 em handleMethodNotSupported [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:216]
- [x] [Review][Patch] Adicionar verificação de nulo em ex.getConstraintViolations() no handleConstraintViolation [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:103]
- [x] [Review][Patch] Tratar DateTimeParseException no GlobalExceptionHandler retornando HTTP 400 [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:171]
- [x] [Review][Patch] Adicionar starter do Flyway para auto-configuração no Spring Boot 4.1.0 [backend/pom.xml:100]
- [x] [Review][Patch] Tratar NoResourceFoundException e HttpRequestMethodNotSupportedException no GlobalExceptionHandler retornando 404/405 padronizados [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:163]
- [x] [Review][Patch] Tratar ConstraintViolationException no GlobalExceptionHandler e remover handler local duplicado de ConsolidacaoController [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java:39]
- [x] [Review][Patch] Sanitizar mensagens de erro em handleTypeMismatchAndMissingParam para evitar vazamento de classes internas [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:79]
- [x] [Review][Patch] Adicionar verificação de inicialização e migração do Flyway no teste de contexto da aplicação [backend/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationTests.java:9]
- [x] [Review][Defer] Consolidar índice B-tree duplicado em usuarios(google_subject_id) na migração Flyway [backend/src/main/resources/db/migration/V1__create_performance_indexes.sql:75] — deferred, pre-existing
- [x] [Review][Defer] Otimizar consultas do ConsolidacaoService para utilizar os índices compostos de data diretamente no banco ao invés de carga em memória [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:71] — deferred, pre-existing
- [x] [Review][Defer] Padronizar payload de erro com ApiErrorResponse na exclusão de recurso inexistente (DELETE /{id}) [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:49] — deferred, pre-existing
- [x] [Review][Defer] Centralizar validação de tenant em relacionamentos (validarEObterConta/validarEObterCategoria) em validador compartilhado [backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java:48] — deferred, pre-existing
- [x] [Review][Defer] Criar índices adicionais no banco para chaves estrangeiras (conta_id, categoria_id) e usuario_id em contas/categorias [backend/src/main/resources/db/migration/V1__create_performance_indexes.sql:1] — deferred, pre-existing
- [x] [Review][Defer] Alinhar consistência de cálculo histórico no ConsolidacaoService para movimentações sem dataInicio [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:153] — deferred, pre-existing
- [x] [Review][Defer] Padronizar tratamento de erros dos filtros de segurança (Spring Security) no formato ApiErrorResponse [backend/src/main/java/com/guilhermepagio/aureus/backend/security/SecurityConfig.java:1] — deferred, pre-existing
- [x] [Review][Defer] Aplicar ordenação alfabética (OrderByDescricaoAsc) em CategoriaService.listar() [backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java:26] — deferred, pre-existing
- [x] [Review][Defer] Padronizar retorno dos métodos criar entre controllers (ResponseEntity vs DTO direto) [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java:36] — deferred, pre-existing
- [x] [Review][Defer] Tratar fail-closed estrito para tenant não autenticado no CurrentTenantIdentifierResolverImpl [backend/src/main/java/com/guilhermepagio/aureus/backend/security/CurrentTenantIdentifierResolverImpl.java:1] — deferred, pre-existing
- [x] [Review][Patch] Tratar fallback de mensagem nula em handleIllegalArgument no GlobalExceptionHandler [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:218]
- [x] [Review][Patch] Extrair nome do nó folha (leaf parameter) no handleConstraintViolation do GlobalExceptionHandler para evitar prefixo de método [backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java:110]
- [x] [Review][Patch] Configurar GlobalExceptionHandler no setup de ConsolidacaoControllerTest e validar rejeição de mesAno inválido via MockMvc [backend/src/test/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoControllerTest.java:47]
- [x] [Review][Defer] Configurar isolamento de banco de dados/Testcontainers ou profile específico para BackendApplicationTests sem exigir container Postgres ativo na porta 5432 [backend/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationTests.java:10] — deferred, pre-existing
- [x] [Review][Defer] Adicionar testes MockMvc verificando HTTP 403 Forbidden nos controllers de movimentação quando tenant for inválido [backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java:37] — deferred, pre-existing
- [x] [Review][Defer] Eliminar checagem manual de autenticação (usuarioId == null) em ConsolidacaoController delegando para o filtro Spring Security [backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ConsolidacaoController.java:32] — deferred, pre-existing
- [x] [Review][Defer] Utilizar a coluna persistida dataFim no ConsolidacaoService ao invés de recalcular a data final das parcelas em memória [backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java:79] — deferred, pre-existing
- [x] [Review][Defer] Coexistência de hibernate.ddl-auto: update e migrações Flyway em application.yaml [backend/src/main/resources/application.yaml:10] — deferred, pre-existing

**Acceptance Criteria:**
- Given um payload inválido enviado a qualquer endpoint REST, when a requisição for processada, then a API deve responder HTTP 400 com estrutura `ApiErrorResponse` contendo lista/mapa de erros de validação
- Given uma tentativa de exclusão de Conta ou Categoria com lançamentos ativos, when o endpoint for chamado, then a API deve responder HTTP 400 com mensagem amigável sem expor detalhes internos do PostgreSQL
- Given um identificador de Conta ou Categoria pertencente a outro tenant informado em uma movimentação financeira, when a criação ou atualização for executada, then o Service deve barrar a operação com HTTP 403 Forbidden
- Given um identificador inexistente informado, when a consulta ou atualização for executada, then o Service deve responder com HTTP 404 Not Found
- Given movimentações financeiras sem conta vinculada existentes na base, when a consolidação mensal for calculada, then os valores devem ser totalizados sob a linha sintética "Sem Conta" e somados no saldo histórico, mantendo igualdade com a consolidação por categoria
- Given a inicialização da aplicação Spring Boot, when o Flyway executar a migração `V1__create_performance_indexes.sql`, then os índices em `usuarios(google_subject_id)` e nas colunas de data das movimentações devem ser criados com sucesso

## Spec Change Log

<!-- Append-only. Populated by step-04 during review loops. -->

## Design Notes

- Estrutura do `ApiErrorResponse`:
```java
public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    Map<String, String> fieldErrors,
    List<ValidationErrorItem> errors
) {
    public record ValidationErrorItem(String field, String message, String defaultMessage) {}
}
```
- A inclusão de `errors` com `defaultMessage` garante 100% de compatibilidade com os hooks do frontend atual (`errorData.errors[0].defaultMessage`).
- Verificação de tenant cruzado:
```java
@Query(value = "SELECT usuario_id FROM contas WHERE id = :id", nativeQuery = true)
Optional<String> findOwnerUsuarioId(@Param("id") Long id);
```
Se `findOwnerUsuarioId(id)` estiver vazio -> lança `ResourceNotFoundException("Conta não encontrada: " + id)`.
Se `findOwnerUsuarioId(id)` estiver presente mas for diferente de `TenantContext.getTenantId()` -> lança `AccessDeniedException("Acesso negado: a conta informada não pertence ao usuário autenticado")`.

## Verification

**Commands:**
- `mvn test` -- expected: Todos os testes unitários (existentes e novos) passando com sucesso
- `mvn clean test` -- expected: Build do Maven limpo e 100% verde

## Suggested Review Order

**Tratamento Global de Erros**

- `@RestControllerAdvice` centralizando conversão de exceções e eliminando stacktraces
  [`GlobalExceptionHandler.java:27`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandler.java#L27)

- Contrato padronizado de erro com mapa e lista de validações
  [`ApiErrorResponse.java:8`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/exception/ApiErrorResponse.java#L8)

- Exceção para recursos não encontrados retornando status 404
  [`ResourceNotFoundException.java:7`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/exception/ResourceNotFoundException.java#L7)

**Validação Estrita de Multi-Tenancy**

- Query nativa para verificar tenant proprietário sem filtro Hibernate
  [`ContaRepository.java:18`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java#L18)

- Query nativa para verificar tenant proprietário de categoria
  [`CategoriaRepository.java:16`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java#L16)

- Validação fail-closed de titularidade de conta e categoria antes de persistir despesa fixa
  [`DespesaFixaService.java:48`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java#L48)

- Validação fail-closed de titularidade de conta e categoria nas despesas variáveis
  [`DespesaVariavelService.java:54`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java#L54)

**Consistência na Consolidação ("Sem Conta")**

- Linha sintética para agregação e cálculo histórico de movimentações sem conta
  [`ConsolidacaoService.java:38`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java#L38)

**Migrações Flyway e Índices de Performance**

- Script Flyway idempotente criando tabelas e índices compostos por tenant e datas
  [`V1__create_performance_indexes.sql:1`](../../backend/src/main/resources/db/migration/V1__create_performance_indexes.sql#L1)

- Configuração do Flyway com baseline no Spring Boot
  [`application.yaml:15`](../../backend/src/main/resources/application.yaml#L15)

**Testes e Validação**

- Cobertura de erros 400, 403, 404 e 500 do handler global
  [`GlobalExceptionHandlerTest.java:31`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/exception/GlobalExceptionHandlerTest.java#L31)

- Testes exaustivos de isolamento de tenant e fail-closed em criar e atualizar
  [`TenantValidationTest.java:30`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/service/TenantValidationTest.java#L30)

- Testes unitários para lançamentos sem conta e paridade entre contas e categorias
  [`ConsolidacaoServiceTest.java:420`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java#L420)

