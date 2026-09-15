---
title: 'Story 5.1: Backend Service Layer e DTOs (Records) para Entidades Financeiras'
type: 'refactor'
created: '2026-09-14'
status: 'done'
baseline_commit: '521ec7f155175a88217dbae75c33a68a108a2d93'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-5-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Os controllers de entidades financeiras legadas (`Conta`, `Categoria`, `DespesaFixa`, `ReceitaFixa`, `DespesaVariavel`, `ReceitaVariavel`) acessam diretamente repositórios JPA e expõem entidades de persistência em parâmetros e retornos de API, violando a separação de responsabilidades e expondo a estrutura interna do banco.

**Approach:** Criar classes dedicadas na camada de serviço (`ContaService`, `CategoriaService`, `DespesaFixaService`, `ReceitaFixaService`, `DespesaVariavelService`, `ReceitaVariavelService`) com `@Transactional`, substituindo todas as entidades JPA nas assinaturas de métodos dos controllers por Java Records imutáveis de Request e Response com sufixo padronizado `*DTO` (`*RequestDTO`, `*ResponseDTO`) e Bean Validation, preservando 100% dos contratos JSON consumidos pelo frontend.

## Boundaries & Constraints

**Always:**
- Toda a lógica de negócio, persistência, ordenação e cálculo (`preencherDataFim`) deve residir exclusivamente nos Services.
- Controllers devem injetar apenas suas respectivas classes de serviço, sem nenhuma dependência de Repositories JPA.
- Nenhuma assinatura de método nos Controllers (`@RequestBody`, retornos de método ou `ResponseEntity<T>`) pode conter entidades JPA `@Entity`.
- DTOs de Request e Response devem ser implementados obrigatoriamente como Java Records imutáveis terminando sempre com o sufixo `*DTO.java`.
- Validações de entrada (`@NotBlank`, `@NotNull`, `@Positive`, `@Size`, `@Min`, `@Max`, `@Digits`, `@Valid`) devem ser declaradas nos Records de Request e validadas nos endpoints com `@Valid`.
- A estrutura e os nomes dos campos JSON enviados e recebidos devem ser rigorosamente idênticos aos atuais para manter compatibilidade transparente com o frontend.

**Ask First:**
- Qualquer alteração na estrutura de contratos JSON ou nos endpoints REST existentes.
- Qualquer alteração nas tabelas do banco de dados ou em entidades JPA além de anotações necessárias para conversão.

**Never:**
- Não expor `@Entity` em DTOs ou controllers.
- Não introduzir dependências diretas de `Repository` dentro dos Controllers.
- Não criar classes/records de DTO sem o sufixo `*DTO.java`.
- Não quebrar o isolamento multi-tenant gerenciado pelo Hibernate `@TenantId`.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Listar contas | `GET /api/contas` | `200 OK` com `List<ContaResponseDTO>` | N/A |
| Criar conta válida | `POST /api/contas` com `ContaRequestDTO("Nubank", "Principal")` | `200 OK` com `ContaResponseDTO(id, "Nubank", "Principal")` | N/A |
| Criar conta inválida | `POST /api/contas` com `descricao: ""` | `400 Bad Request` disparado por Bean Validation | Erro de validação padrão Spring |
| Atualizar conta inexistente | `PUT /api/contas/999` com `ContaRequestDTO("Nova", "")` | `404 Not Found` | N/A |
| Excluir conta com vínculos | `DELETE /api/contas/1` (conta vinculada a lançamentos) | `400 Bad Request` com corpo vazio/mensagem | Captura `DataIntegrityViolationException` |
| Criar despesa fixa válida | `POST /api/despesas-fixas` com `DespesaFixaRequestDTO(descricao, valor, conta: {id}, categoria: {id}, obs, dataInicio)` | `200 OK` com `DespesaFixaResponseDTO(id, ..., ContaResponseDTO, CategoriaResponseDTO, ...)` | `400 Bad Request` se FK violada |
| Criar despesa variável e calcular dataFim | `POST /api/despesas-variaveis` com `quantidadeParcelas: 3, dataInicio: "2024-01-15"` | `200 OK` com `dataInicio: "2024-01-01", dataFim: "2024-03-01"` | N/A |
| Criar receita variável parcela única | `POST /api/receitas-variaveis` com `quantidadeParcelas: 1, dataInicio: "2024-05-10"` | `200 OK` com `dataInicio: "2024-05-01", dataFim: "2024-05-01"` | N/A |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/IdReferenceDTO.java` -- Novo Java Record para representar referências de ID aninhadas em requests (`conta: { id: Long }`, `categoria: { id: Long }`)
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ContaRequestDTO.java` -- Novo DTO Java Record de entrada para Conta com Bean Validation
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ContaResponseDTO.java` -- Novo DTO Java Record de saída para Conta com método `fromEntity`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/CategoriaRequestDTO.java` -- Novo DTO Java Record de entrada para Categoria com Bean Validation
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/CategoriaResponseDTO.java` -- Novo DTO Java Record de saída para Categoria com método `fromEntity`
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaFixaRequestDTO.java` -- Novo DTO Java Record de entrada para DespesaFixa
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaFixaResponseDTO.java` -- Novo DTO Java Record de saída para DespesaFixa
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ReceitaFixaRequestDTO.java` -- Novo DTO Java Record de entrada para ReceitaFixa
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ReceitaFixaResponseDTO.java` -- Novo DTO Java Record de saída para ReceitaFixa
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java` -- Novo DTO Java Record de entrada para DespesaVariavel
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelResponseDTO.java` -- Novo DTO Java Record de saída para DespesaVariavel
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ReceitaVariavelRequestDTO.java` -- Novo DTO Java Record de entrada para ReceitaVariavel
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ReceitaVariavelResponseDTO.java` -- Novo DTO Java Record de saída para ReceitaVariavel
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java` -- Nova classe de serviço para gerenciamento e persistência de Contas
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java` -- Nova classe de serviço para gerenciamento e persistência de Categorias
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java` -- Nova classe de serviço para Despesas Fixas
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java` -- Nova classe de serviço para Receitas Fixas
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java` -- Nova classe de serviço para Despesas Variáveis com cálculo de vigência
- `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java` -- Nova classe de serviço para Receitas Variáveis com cálculo de vigência
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java` -- Refatoração para injetar ContaService e operar exclusivamente com DTOs Records
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java` -- Refatoração para injetar CategoriaService e operar exclusivamente com DTOs Records
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java` -- Refatoração para injetar DespesaFixaService e operar exclusivamente com DTOs Records
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaController.java` -- Refatoração para injetar ReceitaFixaService e operar exclusivamente com DTOs Records
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java` -- Refatoração para injetar DespesaVariavelService e delegar cálculo de datas
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelController.java` -- Refatoração para injetar ReceitaVariavelService e delegar cálculo de datas
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/ContaServiceTest.java` -- Testes unitários para ContaService
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/CategoriaServiceTest.java` -- Testes unitários para CategoriaService
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaFixaServiceTest.java` -- Testes unitários para DespesaFixaService
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaServiceTest.java` -- Testes unitários para ReceitaFixaService
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java` -- Testes unitários para DespesaVariavelService
- `backend/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelServiceTest.java` -- Testes unitários para ReceitaVariavelService

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/IdReferenceDTO.java` -- Criar record para referência aninhada de ID com sufixo DTO -- Reutilizado em requests de lançamentos
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ContaRequestDTO.java` e `ContaResponseDTO.java` -- Criar DTOs records de Conta com Bean Validation -- Desacopla entidade do contrato HTTP
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/CategoriaRequestDTO.java` e `CategoriaResponseDTO.java` -- Criar DTOs records de Categoria com Bean Validation -- Desacopla entidade do contrato HTTP
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaFixaRequestDTO.java` e `DespesaFixaResponseDTO.java` -- Criar DTOs records de Despesa Fixa -- Substitui JPA entity nos contratos
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ReceitaFixaRequestDTO.java` e `ReceitaFixaResponseDTO.java` -- Criar DTOs records de Receita Fixa -- Substitui JPA entity nos contratos
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java` e `DespesaVariavelResponseDTO.java` -- Criar DTOs records de Despesa Variável -- Substitui JPA entity nos contratos
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ReceitaVariavelRequestDTO.java` e `ReceitaVariavelResponseDTO.java` -- Criar DTOs records de Receita Variável -- Substitui JPA entity nos contratos
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java` -- Implementar serviço de Conta com métodos CRUD e transações usando ContaRequestDTO e ContaResponseDTO -- Centraliza regras e isola repositório
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/CategoriaService.java` -- Implementar serviço de Categoria com métodos CRUD e transações usando CategoriaRequestDTO e CategoriaResponseDTO -- Centraliza regras e isola repositório
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java` -- Implementar serviço de Despesa Fixa operando com DTOs records -- Encapsula persistência e resolução de vínculos
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaService.java` -- Implementar serviço de Receita Fixa operando com DTOs records -- Encapsula persistência e resolução de vínculos
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java` -- Implementar serviço de Despesa Variável com cálculo de vigência temporal operando com DTOs records -- Encapsula cálculo de parcelas e persistência
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java` -- Implementar serviço de Receita Variável com cálculo de vigência temporal operando com DTOs records -- Encapsula cálculo de parcelas e persistência
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java` -- Refatorar controller para delegar a ContaService usando ContaRequestDTO e ContaResponseDTO -- Remove acoplamento ao repository e JPA entities
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java` -- Refatorar controller para delegar a CategoriaService usando CategoriaRequestDTO e CategoriaResponseDTO -- Remove acoplamento ao repository e JPA entities
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java` -- Refatorar controller para delegar a DespesaFixaService usando DTOs records -- Remove acoplamento ao repository e JPA entities
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaFixaController.java` -- Refatorar controller para delegar a ReceitaFixaService usando DTOs records -- Remove acoplamento ao repository e JPA entities
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java` -- Refatorar controller para delegar a DespesaVariavelService usando DTOs records -- Remove acoplamento ao repository e regras de cálculo
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ReceitaVariavelController.java` -- Refatorar controller para delegar a ReceitaVariavelService usando DTOs records -- Remove acoplamento ao repository e regras de cálculo
- [x] `backend/src/test/java/com/guilhermepagio/aureus/backend/service/*Test.java` -- Criar testes unitários para os 6 novos Services -- Garante cobertura das regras e validações isoladas

**Acceptance Criteria:**
- Given os controllers e repositories de Contas, Categorias, Despesas e Receitas, when a refatoração for concluída, then nenhuma classe de Controller injeta diretamente nenhum `Repository`.
- Given qualquer método em qualquer um dos 6 controllers, when inspecionada a assinatura do método (parâmetros de entrada e retornos), then nenhuma classe anotada com `@Entity` está presente, utilizando estritamente Java Records imutáveis nomeados com o sufixo `*DTO.java`.
- Given requisições enviadas pelo frontend para qualquer um dos endpoints (`/api/contas`, `/api/categorias`, `/api/despesas-fixas`, `/api/receitas-fixas`, `/api/despesas-variaveis`, `/api/receitas-variaveis`), when a resposta for recebida, then o formato JSON retornado é 100% idêntico e retrocompatível com a especificação existente.
- Given chamadas de criação ou atualização de despesas e receitas variáveis, when processadas pelo respectivo Service, then a data de início é ajustada para o primeiro dia do mês e a data de término calculada exatamente como `dataInicio + (quantidadeParcelas - 1) meses`.

## Spec Change Log

_None._

## Design Notes

Para manter compatibilidade perfeita com os payloads JSON enviados pelo frontend (onde relacionamentos são enviados na forma `conta: { id: 1 }` e `categoria: { id: 2 }`), os Records de Request utilizam:
```java
public record IdReferenceDTO(@NotNull(message = "O ID é obrigatório") Long id) {}
```
Todos os DTOs utilizam estritamente o sufixo `*DTO.java` (`ContaRequestDTO`, `ContaResponseDTO`, `CategoriaRequestDTO`, `CategoriaResponseDTO`, etc.), alinhando-se aos já existentes `ConsolidacaoPorContaDTO` e `ConsolidacaoPorCategoriaDTO`.

No mapeamento de entidade para DTO de resposta, os métodos estáticos `fromEntity(entity)` nos Records extraem os dados de forma defensiva, tratando nulos quando aplicável:
```java
public record ContaResponseDTO(Long id, String descricao, String observacoes) {
    public static ContaResponseDTO fromEntity(Conta conta) {
        if (conta == null) return null;
        return new ContaResponseDTO(conta.getId(), conta.getDescricao(), conta.getObservacoes());
    }
}
```
Nos Services de despesas e receitas, os vínculos com `Conta` e `Categoria` são associados a partir de `contaRepository.findById(request.conta().id())` e `categoriaRepository.findById(request.categoria().id())` ou `getReferenceById(id)`.

## Verification

**Commands:**
- `cd backend && ./mvnw test` -- expected: Todos os testes unitários e de integração existentes e novos passam com sucesso
- `cd frontend && npm test -- --run` -- expected: Testes do frontend continuam passando com 100% de sucesso

## Suggested Review Order

**Contratos e DTOs Imutáveis (Java Records)**

- Contrato comum para referências relacionais aninhadas (`conta: { id }`, `categoria: { id }`) com validação estrita
  [`IdReferenceDTO.java:6`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/IdReferenceDTO.java#L6)

- DTO de entrada de Conta com Bean Validation
  [`ContaRequestDTO.java:7`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ContaRequestDTO.java#L7)

- DTO de saída de Conta com conversão estática defensiva
  [`ContaResponseDTO.java:5`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/ContaResponseDTO.java#L5)

- DTO de entrada de Despesa Fixa com validações monetárias precisas
  [`DespesaFixaRequestDTO.java:14`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaFixaRequestDTO.java#L14)

- DTO de entrada de Despesa Variável com validações de parcelas e limites
  [`DespesaVariavelRequestDTO.java:18`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/dto/DespesaVariavelRequestDTO.java#L18)

**Camada de Serviço e Regras de Negócio (Service Layer)**

- Serviço de contas encapsulando operações CRUD e isolamento transacional
  [`ContaService.java:19`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/ContaService.java#L19)

- Serviço de despesas fixas com resolução de vínculos e ordenação alfabética
  [`DespesaFixaService.java:23`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaFixaService.java#L23)

- Serviço de despesas variáveis com cálculo e ajuste de vigência temporal
  [`DespesaVariavelService.java:24`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java#L24)

- Normalização de início no dia 1 e projeção da data de término das parcelas
  [`DespesaVariavelService.java:39`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java#L39)

**Controllers Refatorados (Apresentação Desacoplada)**

- Controller de Contas operando exclusivamente com DTOs e sem repositório JPA
  [`ContaController.java:24`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java#L24)

- Controller de Despesas Fixas sem entidades JPA em parâmetros ou retornos
  [`DespesaFixaController.java:24`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java#L24)

- Controller de Despesas Variáveis delegando cálculo de vigência ao serviço
  [`DespesaVariavelController.java:24`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java#L24)

**Suíte de Testes Automatizados**

- Testes unitários do serviço de Conta verificando CRUD e isolamento
  [`ContaServiceTest.java:26`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/service/ContaServiceTest.java#L26)

- Testes de vigência temporal e edge cases de cálculo de despesa variável
  [`DespesaVariavelServiceTest.java:29`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java#L29)

- Testes MockMvc de controller garantindo validações de schema e status HTTP
  [`DespesaFixaControllerTest.java:35`](../../backend/src/test/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaControllerTest.java#L35)

