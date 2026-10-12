# Epic 6 Context: Testes Automatizados e Qualidade Contínua (Testing Strategy)

<!-- Compiled from planning artifacts. Edit freely. Regenerate with compile-epic-context if planning docs change. -->

## Goal

Garantir a confiabilidade, robustez matemática, isolamento de segurança e integridade de regressão do Aureus V1 através de uma estratégia de testes em três camadas (Unitários Backend com JUnit 5/Mockito, Integração/Multitenancy Backend com MockMvc e Testcontainers PostgreSQL via Maven Failsafe, e Componentes Frontend com Vitest/Testing Library). O épico constrói a infraestrutura de validação contínua e a documentação canônica de testes, assegurando que cálculos financeiros de 24 meses, isolamento entre tenants e fluxos interativos de UI funcionem com 100% de previsibilidade sem dependência de ambientes externos manuais.

## Stories

- Story 6.1: Testes Unitários de Domínio e Serviços Financeiros (Backend)
- Story 6.2: Testes de Integração REST e Isolamento Multi-Tenancy (Backend)
- Story 6.3: Testes de Componentes e Fluxos Críticos no Frontend (Frontend)
- Story 6.4: Infraestrutura de Validação Contínua Local e Documentação Canônica de Testes

## Requirements & Constraints

- **Isolamento Completo da Camada Unitária**: Testes unitários do backend (`*Test.java`) devem testar lógica de negócio, serviços e domínio de forma pura, usando JUnit 5 e Mockito, executando em milissegundos via `mvn test` (Surefire) sem carregar o contexto Spring Boot completo e sem exigir banco de dados ou Docker ativo.
- **Precisão Matemática e Edge Cases**: Cobertura estrita das regras financeiras: valor total de parcelamentos (`Valor Parcela × Nº Parcelas`), projeção temporal da última parcela (`Primeira Parcela + (Nº Parcelas - 1) meses`), regra de parcela única (`Nº Parcelas = 1`), agregações e agrupamentos do `ConsolidacaoService` (R$ e %), prevenção de divisão por zero (`NaN`/`Infinity`) quando total de despesas for zero, saldos negativos e histórico acumulado pré-grade.
- **Separação de Ciclos de Teste no Maven**: Configuração canônica no `pom.xml` diferenciando testes unitários rápidos (`*Test.java` com `maven-surefire-plugin` na fase `test`) de testes de integração pesados (`*IT.java` com `maven-failsafe-plugin` na fase `verify`).
- **Isolamento de Banco e Multi-Tenancy Real**: Testes de integração (`*IT.java`) devem utilizar Testcontainers (PostgreSQL) para validar persistência real, migrações Flyway e isolamento multi-tenant lógico (usuário A nunca acessa registros do usuário B, retornando `404 Not Found`).
- **Validação de Componentes Frontend**: Testes em Vitest e React Testing Library (`npm test`) validando reatividade de formulários (cálculos em tempo real de parcelas), acessibilidade de modais (`Focus Trap`, ESC, fechamento), filtros de data (`MonthPicker`, `DatePicker`), alternância de filtros e confirmação explícita de exclusão.
- **Pipeline Unificado e Reprodutibilidade**: Script local de validação unificada e documentação canônica de testes no repositório descrevendo como executar e manter cada suíte.

## Technical Decisions

- **JUnit 5 + Mockito para Services**: Classes de serviço criadas no Épico 5 (`ContaService`, `CategoriaService`, `DespesaFixaService`, `ReceitaFixaService`, `DespesaVariavelService`, `ReceitaVariavelService`, `ConsolidacaoService`) devem ser testadas com mocks estritos de seus respectivos repositórios e colaboradores.
- **Testcontainers PostgreSQL para Integração**: Testes de integração utilizam container PostgreSQL efêmero gerenciado pelo Testcontainers, garantindo compatibilidade exata com queries e migrações Flyway sem necessidade de Postgres pré-configurado na máquina do desenvolvedor.
- **Surefire vs Failsafe**:
  - `mvn test`: Roda apenas testes unitários rápidos (`*Test.java`).
  - `mvn verify`: Roda testes unitários (`*Test.java`) seguidos pelos testes de integração (`*IT.java`).
- **Vitest + React Testing Library + jsdom**: Suíte frontend configurada para renderização isolada de componentes, mocking de requisições de API via cliente centralizado ou stubs, e verificação de comportamento acessível.

## Cross-Story Dependencies

- **Pré-requisitos**: Depende da arquitetura saneada no Épico 5 (Service Layer, DTOs Records, validação de titularidade multi-tenant, cliente de API centralizado e formulários unificados).
- **Sequência Interna**:
  - Story 6.1 (Unitários Backend): Base de testes rápidos de domínio e services sem dependência externa.
  - Story 6.2 (Integração Backend): Separação Surefire/Failsafe, Testcontainers e isolamento multi-tenant REST.
  - Story 6.3 (Componentes Frontend): Suíte Vitest cobrindo formulários e componentes refatorados.
  - Story 6.4 (Pipeline e Documentação): Script unificado orquestrando todas as suítes e documentação canônica.
