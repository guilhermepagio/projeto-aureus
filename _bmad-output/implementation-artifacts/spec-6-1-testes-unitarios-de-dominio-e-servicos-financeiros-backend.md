---
title: 'Story 6.1: Testes Unitários de Domínio e Serviços Financeiros (Backend)'
type: 'feature'
created: '2026-10-04'
status: 'done'
baseline_commit: '60e1210bfd40bac4d00c6c9311d0b41161ccaf50'
review_loop_iteration: 0
context:
  - '_bmad-output/implementation-artifacts/epic-6-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** A camada de serviços e o modelo de domínio financeiro necessitam de cobertura unitária rigorosa e isolada para fórmulas matemáticas (valor total, projeção temporal da última parcela, parcela única, consolidação e proporções percentuais) e garantia de que `mvn test` execute em milissegundos sem depender de banco de dados ou Docker.

**Approach:** Fortalecer os métodos de cálculo de domínio em `DespesaVariavel`, `ReceitaVariavel` e `ConsolidacaoService`, adicionar testes unitários isolados com JUnit 5/Mockito para todos os 7 serviços e entidades de domínio, isolar a classe de teste de integração em `BackendApplicationIT` e configurar o Surefire para executar apenas testes unitários (`*Test.java`).

## Boundaries & Constraints

**Always:**
- Testes unitários (`*Test.java`) devem rodar via Maven Surefire (`mvn test`) de forma isolada em milissegundos, sem requisições de rede, sem Docker e sem inicialização de banco de dados externo.
- Todos os testes de Services devem utilizar Mockito para isolamento de repositories e collaborators (`@ExtendWith(MockitoExtension.class)`).
- Cálculos financeiros de proporção devem prevenir divisão por zero quando o total for zero ou nulo, retornando `0.00` em vez de lançar exceção aritmética.
- A regra de parcela única (`quantidadeParcelas = 1`) deve garantir `valorTotal = valorParcela` e `dataFim = dataInicio`.
- Classes que necessitam de contexto Spring Boot ou banco (`@SpringBootTest`) devem adotar o sufixo `IT.java` para o ciclo de integração da Story 6.2.

**Ask First:**
- Qualquer alteração nos contratos JSON expostos pelos DTOs Records existentes (`DespesaVariavelResponseDTO`, `ConsolidacaoPorCategoriaDTO`, etc.).

**Never:**
- Não usar `@SpringBootTest` em arquivos `*Test.java` da suíte unitária.
- Não adicionar comentários no código com referências a histórias, tarefas ou prompts (ex: `# Story 6.1`).
- Não adicionar dependências externas desnecessárias no `pom.xml`.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Valor total parcelado | `valorParcela = 150.50`, `quantidadeParcelas = 10` | `valorTotal = 1505.00` | N/A |
| Parcela única | `valorParcela = 250.00`, `quantidadeParcelas = 1`, `dataInicio = 2024-05-15` | `valorTotal = 250.00`, `dataInicio = 2024-05-01`, `dataFim = 2024-05-01` | N/A |
| Última parcela multi-mês | `dataInicio = 2024-01-10`, `quantidadeParcelas = 12` | `dataFim = 2024-12-01` | N/A |
| Percentual categoria com despesas | `valorCategoria = 250.00`, `totalDespesas = 1000.00` | `25.00%` | N/A |
| Percentual divisão por zero | `valorCategoria = 0.00`, `totalDespesas = 0.00` | `0.00%` (sem `ArithmeticException`) | Retorna `0.00` tratado |
| Percentual total nulo ou negativo | `valorCategoria = 50.00`, `totalDespesas = null` ou `<= 0` | `0.00%` | Retorna `0.00` tratado |
| Saldo histórico pré-grade negativo | Despesas históricas > Receitas históricas | Saldo negativo exato com escala 2 (`RoundingMode.HALF_UP`) | N/A |
| Grade 24 meses sem movimentações | Zero despesas e zero receitas no período | Listas de contas/categorias vazias ou zeradas, saldo pré-grade `0.00` | N/A |

</frozen-after-approval>

## Code Map

- `backend-api/pom.xml` -- Configuração do `maven-surefire-plugin` restringindo inclusão a `**/*Test.java` e excluindo `**/*IT.java`
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java` -- Métodos de domínio para cálculo de `getValorTotal()` e `getUltimaParcela()`
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java` -- Métodos de domínio para cálculo de `getValorTotal()` e `getUltimaParcela()`
- `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Método utilitário de cálculo percentual seguro `calcularPercentual(valor, total)`
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationIT.java` -- Renomeação e isolamento do teste de contexto Spring Boot para ciclo IT
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavelTest.java` -- Testes unitários das regras matemáticas de despesas variáveis e parcela única
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavelTest.java` -- Testes unitários das regras matemáticas de receitas variáveis e parcela única
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java` -- Testes unitários cobrindo percentuais, divisão por zero, agregações e saldos negativos
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/ContaServiceTest.java` -- Testes unitários de ContaService
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/CategoriaServiceTest.java` -- Testes unitários de CategoriaService
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaFixaServiceTest.java` -- Testes unitários de DespesaFixaService
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaFixaServiceTest.java` -- Testes unitários de ReceitaFixaService
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelServiceTest.java` -- Testes unitários de DespesaVariavelService
- `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelServiceTest.java` -- Testes unitários de ReceitaVariavelService

## Tasks & Acceptance

**Execution:**
- [x] `backend-api/pom.xml` -- Configurar includes/excludes no `maven-surefire-plugin` -- Assegurar que `mvn test` execute exclusivamente testes unitários rápidos (`*Test.java`)
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java` -- Adicionar métodos `getValorTotal()` e `getUltimaParcela()` -- Encapsular lógica matemática de parcelamento no domínio
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java` -- Adicionar métodos `getValorTotal()` e `getUltimaParcela()` -- Encapsular lógica matemática de parcelamento no domínio
- [x] `backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java` -- Adicionar método estático `calcularPercentual(BigDecimal, BigDecimal)` -- Prevenir divisão por zero e padronizar cálculo de percentuais
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavelTest.java` -- Criar suíte de testes unitários de domínio para `DespesaVariavel` -- Validar valor total, última parcela, parcela única e edge cases nulos/zerados
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavelTest.java` -- Criar suíte de testes unitários de domínio para `ReceitaVariavel` -- Validar valor total, última parcela, parcela única e edge cases nulos/zerados
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java` -- Adicionar testes de percentuais de categoria e divisão por zero -- Cobrir todos os cenários da matriz analítica de consolidação
- [x] `backend-api/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationIT.java` -- Garantir renomeação da classe e arquivo de integração -- Desacoplar do ciclo `mvn test`

**Acceptance Criteria:**
- Given os Services de Contas, Categorias, Despesas, Receitas e Consolidação, when os testes unitários são executados via JUnit 5 e Mockito, then todos os 7 services e entidades de domínio devem possuir cobertura de testes unitários isolados sem depender de infraestrutura externa.
- Given uma movimentação variável, when calculados o valor total e a data final, then o valor total deve ser `valorParcela × quantidadeParcelas` e a última parcela deve ser `dataInicio + (quantidadeParcelas - 1) meses`.
- Given uma movimentação com quantidade de parcelas igual a 1, when processada, then a data de início deve ser truncada para o dia 1 do mês e a data fim deve ser idêntica à data de início.
- Given o cálculo de percentuais da consolidação por categoria, when o total de despesas for zero ou nulo, then o cálculo deve retornar `0.00%` sem disparar `ArithmeticException` ou gerar `NaN`.
- Given o comando `mvn test` executado na raiz de `backend-api`, when finalizado, then todos os testes unitários (`*Test.java`) devem passar com sucesso (`BUILD SUCCESS`) em milissegundos sem exigir container Docker ativo.

## Spec Change Log

## Design Notes

Método de prevenção de divisão por zero em percentual:
```java
public static BigDecimal calcularPercentual(BigDecimal valorCategoria, BigDecimal totalDespesas) {
    if (totalDespesas == null || totalDespesas.compareTo(BigDecimal.ZERO) <= 0 
            || valorCategoria == null || valorCategoria.compareTo(BigDecimal.ZERO) <= 0) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    return valorCategoria.multiply(BigDecimal.valueOf(100))
            .divide(totalDespesas, 2, RoundingMode.HALF_UP);
}
```

Métodos de domínio em `DespesaVariavel` e `ReceitaVariavel`:
```java
public BigDecimal getValorTotal() {
    if (valorParcela == null || quantidadeParcelas == null) {
        return BigDecimal.ZERO;
    }
    return valorParcela.multiply(BigDecimal.valueOf(quantidadeParcelas));
}

public LocalDate getUltimaParcela() {
    if (dataInicio == null || quantidadeParcelas == null || quantidadeParcelas <= 0) {
        return null;
    }
    return dataInicio.withDayOfMonth(1).plusMonths(quantidadeParcelas - 1);
}
```

## Verification

**Commands:**
- `./mvnw test` -- expected: `BUILD SUCCESS` com 0 falhas e 0 erros em milissegundos sem container Docker ativo
- `./mvnw test-compile` -- expected: `BUILD SUCCESS` sem warnings ou erros de compilação

## Suggested Review Order

**Regras Matemáticas e Domínio**

- Métodos de domínio para valor total e projeção da última parcela com @Transient e @JsonIgnore
  [`DespesaVariavel.java:185`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java#L185)

- Métodos de domínio para valor total e projeção da última parcela em receitas variáveis
  [`ReceitaVariavel.java:162`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavel.java#L162)

- Cálculo seguro de percentual com proteção contra divisão por zero e arredondamento HALF_UP
  [`ConsolidacaoService.java:347`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoService.java#L347)

- Delegação do cálculo de data final para o método de domínio
  [`DespesaVariavelService.java:51`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/DespesaVariavelService.java#L51)

- Delegação do cálculo de data final para o método de domínio
  [`ReceitaVariavelService.java:51`](../../backend-api/src/main/java/com/guilhermepagio/aureus/backend/service/ReceitaVariavelService.java#L51)

**Configuração do Ciclo de Testes e Isolamento**

- Restrição de Surefire para executar exclusivamente testes unitários e excluir *IT.java
  [`pom.xml:142`](../../backend-api/pom.xml#L142)

- Isolamento do teste com contexto Spring Boot e Flyway para o ciclo de integração
  [`BackendApplicationIT.java:10`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/BackendApplicationIT.java#L10)

**Suíte de Testes Automatizados**

- Suíte de testes unitários de regras matemáticas e edge cases de despesa variável
  [`DespesaVariavelTest.java:12`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavelTest.java#L12)

- Suíte de testes unitários de regras matemáticas e edge cases de receita variável
  [`ReceitaVariavelTest.java:12`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/domain/ReceitaVariavelTest.java#L12)

- Testes de proporções percentuais, divisão por zero e grade de 24 meses sem movimentações
  [`ConsolidacaoServiceTest.java:600`](../../backend-api/src/test/java/com/guilhermepagio/aureus/backend/service/ConsolidacaoServiceTest.java#L600)
