# 🧪 Arquitetura de Testes e Estratégia de Qualidade Contínua — Aureus V1

Este documento formaliza a arquitetura canônica de testes automatizados, a matriz qualitativa de cobertura e as decisões de engenharia de qualidade do **Aureus V1**. Ele serve como fonte de verdade para o desenvolvedor solo e agentes de IA sobre como as camadas de teste são estruturadas, mantidas e validadas.

---

## 1. Visão Geral e Filosofia de Qualidade

O **Aureus** é uma plataforma fullstack para gestão de finanças pessoais de alta precisão. Por lidar diretamente com planejamento orçamentário plurianual (horizonte de 24 meses), lançamentos parcelados e agregação patrimonial, o sistema exige tolerância zero a regressões de cálculo, falhas de persistência e vazamento de dados entre usuários.

Como o projeto é mantido por um **desenvolvedor solo** sob o paradigma **AI-First (BMad Method)**, a estratégia de qualidade adota os seguintes pilares fundamentais:

1. **Velocidade de Feedback (Inner Loop Ágil):** A validação contínua de rotina na máquina do desenvolvedor deve executar em menos de 30 segundos, sem requerer a inicialização de contêineres pesados ou dependências externas complexas.
2. **Determinismo Absoluto (Zero Flakiness):** Testes unitários e de componentes rodam de forma hermética em memória, eliminando oscilações de rede, portas abertas ou estados compartilhados.
3. **Isolamento Lógico Zero Trust (Multi-Tenancy Estrito):** Toda operação de leitura ou escrita valida estritamente o titular do recurso autenticado via JWT. Um usuário nunca deve acessar, modificar ou confirmar a existência de dados de outro inquilino.
4. **Fidelidade Real de Infraestrutura:** Testes de integração utilizam a versão exata do PostgreSQL via **Testcontainers**, assegurando que migrações Flyway, constraints relacionais e triggers operem com paridade idêntica à de produção.

---

## 2. A Arquitetura de Testes em Três Camadas

A pirâmide de testes do Aureus V1 é distribuída em três camadas complementares e bem delimitadas:

```
                  ┌────────────────────────────────────────┐
                  │    Camada 3: Componentes Frontend      │
                  │  (Vitest + React Testing Library)      │
                  │  Reatividade, DOM Virtual, UX e A11y   │
                  └───────────────────┬────────────────────┘
                                      │
                  ┌───────────────────▼────────────────────┐
                  │    Camada 2: Integração Backend        │
                  │   (MockMvc + Testcontainers Postgres)  │
                  │   Contratos REST, Flyway e Multi-Tenant│
                  └───────────────────┬────────────────────┘
                                      │
                  ┌───────────────────▼────────────────────┐
                  │    Camada 1: Unitários de Domínio      │
                  │       (JUnit 5 + Mockito Puro)         │
                  │   Lógica Matemática, Services e Regras │
                  └────────────────────────────────────────┘
```

---

### Camada 1: Testes Unitários de Domínio e Serviços (Backend)

* **Objetivo:** Validar as regras de negócio financeiras, cálculos de parcelamento, agregações de consolidação e validações lógicas sem sobrecarga de infraestrutura.
* **Ferramentas:** JUnit 5 (Jupiter), Mockito (`MockitoExtension`), AssertJ.
* **Orquestrador de Build:** `maven-surefire-plugin` (fase `test` do Maven).
  * Padrão de inclusão: `**/Test*.java`, `**/*Test.java`, `**/*TestCase.java`.
  * Padrão de exclusão: `**/*IT.java` (reservados para integração).
  * Argumentos JVM: `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true`.
* **Características Técnicas:**
  * Execução pura na JVM (tempo de execução: milissegundos).
  * **Não levanta** o contexto Spring Boot (`@SpringBootTest` não é utilizado nesta camada).
  * Repositórios e colaboradores externos são isolados com `@Mock`, e a classe sob teste é instanciada com `@InjectMocks`.
* **Classes e Suítes Principais:**
  * `ContaServiceTest`: Validação de saldo, unicidade, titularidade e proteção de vínculo.
  * `CategoriaServiceTest`: CRUD e integridade de tipos de categoria por usuário.
  * `DespesaFixaServiceTest` & `ReceitaFixaServiceTest`: Vigência temporal e lançamentos recorrentes.
  * `DespesaVariavelServiceTest` & `ReceitaVariavelServiceTest`: Cálculo de valor total por parcela, projeção de última parcela e fallback para parcela única.
  * `ConsolidacaoServiceTest`: Agrupamento e agregação da grade de 24 meses por contas, categorias (R$ e %), prevenção de divisão por zero e cálculo de sobra histórica acumulada.
  * `TenantValidationTest`: Validação explícita de relacionamentos cruzados (garantindo que conta e categoria pertençam ao mesmo usuário autenticado).
  * `*ControllerTest` com `@WebMvcTest`: Validação de DTOs, serialização JSON e anotações `@Valid` sem dependência de banco.

---

### Camada 2: Testes de Integração e Isolamento Multi-Tenancy (Backend)

* **Objetivo:** Validar o ciclo de vida completo da API REST, execução real das migrações Flyway, constraints de chave estrangeira (`ON DELETE RESTRICT`) e o isolamento rígido entre múltiplos inquilinos.
* **Ferramentas:** Spring Boot Test (`@SpringBootTest`), MockMvc (`@AutoConfigureMockMvc`), Testcontainers PostgreSQL (`@Testcontainers`, `PostgreSQLContainer`), Flyway.
* **Orquestrador de Build:** `maven-failsafe-plugin` (fases `integration-test` e `verify` do Maven).
  * Padrão de inclusão: `**/*IT.java`.
* **Características Técnicas:**
  * Herança da classe base `AbstractIntegrationTest`, que inicializa um contêiner PostgreSQL efêmero compartilhado para a suíte.
  * As migrações do Flyway são aplicadas em um esquema real de banco de dados no contêiner.
  * **Execução in-memory do MockMvc** (`webEnvironment = SpringBootTest.WebEnvironment.MOCK`): as requisições HTTP REST são despachadas diretamente através da cadeia de filtros do Spring Security e dos controllers sem a sobrecarga de vinculação a portas de rede físicas (`RANDOM_PORT`), garantindo testes herméticos, determinísticos e rápidos.
  * **Isolamento de Dados Zero-Leakage (Multi-Tenancy Dinâmico)**: o isolamento entre testes não depende de truncamento lento do banco de dados, mas da geração de inquilinos com identificadores únicos (UUIDs/e-mails distintos) para cada cenário (`Tenant A`, `Tenant B`), eliminando colisões de estado e vazamentos de dados entre execuções concorrentes ou sequenciais.
  * Executado de forma isolada do fluxo diário rápido via comando dedicado (`./mvnw verify` ou `./scripts/run-unit-tests.sh --with-integration`).
* **Suítes de Integração Implementadas:**
  * `MultiTenancyIT`:
    * Garante que o **Usuário B** não consegue ler, atualizar ou excluir recursos (contas, categorias, lançamentos fixos e variáveis) criados pelo **Usuário A**.
    * Acesso cross-tenant retorna estritamente **`404 Not Found`**, impedindo inferência de existência de recursos alheios.
  * `RestContractAndIntegrityIT`:
    * Valida que regras relacionais (`ON DELETE RESTRICT`) impedem a exclusão de contas e categorias com lançamentos atrelados, gerando respostas de erro tratadas pelo `GlobalExceptionHandler` (`400 Bad Request` ou `409 Conflict`).
  * `SecurityIT`:
    * Valida rejeição de requisições desprovidas de autenticação ou com tokens JWT corrompidos/expirados (`401 Unauthorized`).
  * `BackendApplicationIT`:
    * Validação do carregamento completo do contexto de inicialização do Spring Boot (`contextLoads`).

---

### Camada 3: Testes de Componentes e Fluxos Críticos (Frontend)

* **Objetivo:** Garantir a fidelidade da experiência do usuário, reatividade de formulários financeiros, acessibilidade (A11y) e estabilidade da navegação temporal.
* **Ferramentas:** Vitest, React Testing Library, jsdom, `@testing-library/user-event`, TypeScript.
* **Comando de Execução:** `npm test` (`vitest run --environment jsdom` em `frontend-web/`).
* **Características Técnicas:**
  * Renderização em DOM virtual (jsdom) rápida e hermética, sem overhead de navegador real.
  * Simulação de eventos de usuário (`userEvent`) para digitação, tabulação e cliques.
  * Interceptação e mocks de chamadas de API através do cliente HTTP centralizado (`apiClient`).
* **Componentes e Módulos Cobertos:**
  * **Formulários e Reatividade:**
    * `MovimentacaoForm.test.tsx` e `parcelamentoUtils.test.ts`: Recálculo em tempo real de `Valor Total = Valor Parcela × Nº Parcelas` e projeção da data da última parcela durante a digitação.
    * Acessibilidade de validação de formulários (`aria-invalid`, `aria-describedby`, `role="alert"`).
  * **Painel de Consolidação de 24 Meses:**
    * `ConsolidacaoGrid.test.tsx`: Renderização das colunas mensais com agrupamento anual em supercabeçalho, colapso/expansão de seções e ocultação de contas zeradas.
    * `BlocoCategorias.test.tsx`: Exibição proporcional de percentuais, ordenação e salvaguarda contra divisão por zero (`0%` quando o total for zero).
    * `BlocoResumo.test.tsx`: Apuração do total de despesas, receitas, sobra do mês e transporte da sobra retroativa acumulada.
    * `ConsolidacaoToolbar.test.tsx`: Seleção e navegação entre meses e anos.
  * **Componentes de UI e Acessibilidade:**
    * `Modal.test.tsx`: Confinamento de foco (*focus trap*), fechamento pela tecla `ESC` e clique no backdrop.
    * `MonthPicker.test.tsx` e `DatePicker.test.tsx`: Seleção de datas e formatação pt-BR.
    * `DeleteConfirmModal.test.tsx`: Confirmação explícita de exclusão para evitar operações destrutivas acidentais.
    * `EmptyState.test.tsx` e `ActionMenu.test.tsx`: Estados visuais e menus de ações.
  * **Páginas e Resiliência:**
    * `ContasPage.test.tsx`, `DespesasVariaveisPage.test.tsx`, `ReceitasVariaveisPage.test.tsx`: Fluxos completos de CRUD e listagem.
    * `ErrorBoundary.test.tsx`: Captura e exibição de tela de recuperação em falhas de renderização React.
    * `apiClient.test.ts`: Interceptação de respostas de erro, injeção de credenciais e comunicação resiliente.

---

## 3. Matriz Qualitativa de Cobertura

A tabela a seguir consolida as principais regras de negócio, requisitos de segurança e comportamentos de UX, indicando a camada responsável e a evidência de teste implementada:

| Domínio / Dimensão | Requisito / Regra Coberta | Camada Responsável | Arquivo de Teste de Referência |
| :--- | :--- | :--- | :--- |
| **Matemática Financeira** | Cálculo do Valor Total do Parcelamento (`Valor × Parcelas`) | Unitário Backend & Frontend | `DespesaVariavelTest.java`, `parcelamentoUtils.test.ts` |
| **Matemática Financeira** | Projeção temporal da última parcela (`Data + (N - 1) meses`) | Unitário Backend & Frontend | `DespesaVariavelTest.java`, `parcelamentoUtils.test.ts` |
| **Matemática Financeira** | Regra de Parcela Única (`N = 1` comporta-se como à vista) | Unitário Backend | `DespesaVariavelServiceTest.java` |
| **Matemática Financeira** | Prevenção de divisão por zero (evitar `NaN`/`Infinity` quando total gasto for 0) | Unitário Backend & Componente | `ConsolidacaoServiceTest.java`, `BlocoCategorias.test.tsx` |
| **Matemática Financeira** | Apuração de Sobra Histórica Acumulada pré-grade de 24 meses | Unitário Backend & Componente | `ConsolidacaoServiceTest.java`, `BlocoResumo.test.tsx` |
| **Matemática Financeira** | Agrupamento de receitas e despesas por conta e categoria nos 24 meses | Unitário Backend & Componente | `ConsolidacaoServiceTest.java`, `ConsolidacaoGrid.test.tsx` |
| **Segurança & Multi-Tenant** | Extração de identidade do tenant via token JWT no `TenantContext` | Integração Backend | `SecurityIT.java`, `MultiTenancyIT.java` |
| **Segurança & Multi-Tenant** | Validação de titularidade cruzada (Conta e Categoria do mesmo usuário) | Unitário Backend | `TenantValidationTest.java` |
| **Segurança & Multi-Tenant** | Isolamento total de inquilinos (Usuário B não acessa recursos de A, retornando `404`) | Integração Backend | `MultiTenancyIT.java` |
| **Segurança & Multi-Tenant** | Rejeição de tokens expirados, ausentes ou adulterados (`401 Unauthorized`) | Integração Backend | `SecurityIT.java` |
| **Integridade Relacional** | Bloqueio de exclusão de Conta/Categoria em uso (`ON DELETE RESTRICT`) | Integração Backend | `RestContractAndIntegrityIT.java` |
| **UX & Interatividade** | Recálculo dinâmico em tempo real de parcelas durante preenchimento | Componente Frontend | `MovimentacaoForm.test.tsx` |
| **UX & Interatividade** | Acessibilidade de modais com Focus Trap e fechamento via tecla `ESC` | Componente Frontend | `Modal.test.tsx` |
| **UX & Interatividade** | Mensagens de validação acessíveis vinculadas com `role="alert"` | Componente Frontend | `MovimentacaoForm.test.tsx` |
| **UX & Interatividade** | Confirmação explícita de exclusão de lançamentos financeiros | Componente Frontend | `DeleteConfirmModal.test.tsx` |
| **UX & Interatividade** | Navegação temporal por mês e colapso/expansão de seções da consolidação | Componente Frontend | `ConsolidacaoToolbar.test.tsx`, `ConsolidacaoGrid.test.tsx` |

---

## 4. Registro Formal de Decisão Técnica: Adiamento dos Testes E2E (Playwright/Cypress)

### Contexto e Trade-Offs

Durante o planejamento do Épico 6, foi avaliada a inclusão de uma suíte de testes End-to-End (E2E) baseada em navegadores reais (Playwright ou Cypress). Foi formalizada a decisão de **adiar a implementação de testes E2E para fases pós-V1**.

### Racional Técnico da Decisão

1. **Assimetria de Custo-Benefício para o Desenvolvedor Solo:**
   * Testes E2E com automação de navegador introduzem alto custo de manutenção, lentidão acentuada no ciclo de feedback local (minutos versus segundos) e propensão histórica a falsos positivos e instabilidades (*flakiness*).
   * O fluxo de autenticação da aplicação depende de **Google OAuth 2.0 (OIDC)** com cookies de sessão HttpOnly. Automatizar logins reais com provedores externos via navegador acarreta riscos de bloqueios por CAPTCHA e taxa de requisições, exigindo a construção e manutenção de provedores de Mock OIDC artificiais complexos.
2. **Cobertura Efetiva Já Provida pelas 3 Camadas:**
   * A suíte unitária de domínio garante 100% da integridade matemática de projeção e parcelamento.
   * A suíte de integração com **Testcontainers** garante a fidelidade ponta a ponta dos contratos HTTP REST, segurança JWT, transações de banco de dados e constraints relacionais do PostgreSQL.
   * A suíte de componentes do **React Testing Library** valida fielmente as interações de UI, eventos de digitação, recálculos em tempo real, árvore de acessibilidade e renderização de DOM no ambiente virtual do jsdom.
   * Não há lacuna crítica de negócio ou segurança no V1 que justifique a sobrecarga operacional imediata do Playwright.

### Critérios e Roadmap para Adoção Pós-V1

A introdução de testes E2E com Playwright será revisitada caso os seguintes marcos sejam atingidos:
* Disponibilização de pipeline de CI/CD automatizado em runners gerenciados na nuvem (ex.: GitHub Actions);
* Implementação de ambiente de testes com suporte a Mock OIDC dedicado para autenticação automatizada sem dependência do Google;
* Necessidade de validação de regressão visual entre navegadores heterogêneos (WebKit, Chromium, Firefox) e plataformas móveis.

---

## 5. Guia Prático do Desenvolvedor Solo

### Comandos Rápidos do Dia a Dia

Para manter a produtividade máxima durante o desenvolvimento iterativo, execute as suítes de forma granular conforme a alteração em andamento:

| Objetivo | Comando | Diretório de Execução | Tempo Típico |
| :--- | :--- | :--- | :--- |
| **Backend Unitário** | `./mvnw test` | `backend-api/` | ~10-15s |
| **Backend Integração** | `./mvnw verify` *(requer Docker)* | `backend-api/` | ~25-40s |
| **Frontend Testes** | `npm test` | `frontend-web/` | ~5-10s |
| **Frontend Linter** | `npm run lint` | `frontend-web/` | < 1s |
| **Frontend Build/Tipos** | `npm run build` | `frontend-web/` | ~2-3s |

#### Execução Granular de Testes Individuais

Durante ciclos de TDD, desenvolvimento focado ou depuração pontual, execute apenas o teste ou classe de interesse para reduzir o tempo de feedback para poucos segundos:

| Cenário | Comando | Diretório de Execução |
| :--- | :--- | :--- |
| **Classe de Teste Unitário (Backend)** | `./mvnw test -Dtest=ContaServiceTest` | `backend-api/` |
| **Método Específico de Teste (Backend)** | `./mvnw test -Dtest=ContaServiceTest#deveValidarTitularidadeDaConta` | `backend-api/` |
| **Classe de Teste de Integração (Backend)** | `./mvnw verify -Dit.test=MultiTenancyIT` | `backend-api/` |
| **Arquivo Específico de Componente (Frontend)** | `npx vitest run src/pages/Contas/components/ContaFormModal.test.tsx` | `frontend-web/` |
| **Modo Interativo com Hot Reload (Frontend)** | `npx vitest` | `frontend-web/` |

---

### O Script de Validação Contínua Local (`scripts/run-unit-tests.sh`)

O repositório disponibiliza o script `scripts/run-unit-tests.sh` para orquestrar todas as validações com feedback visual, fail-fast e cálculo de duração. O script pode ser executado a partir de qualquer diretório:

#### 1. Execução Padrão (Recomendada para Rotina Diária)

Executa ordenadamente as 4 etapas que não exigem Docker ativo (duração média: ~20-25s):

```bash
./scripts/run-unit-tests.sh
```

**Etapas executadas:**
1. `[1/4]` Backend: Testes unitários (`./mvnw test`)
2. `[2/4]` Frontend: Linter com Oxlint (`npm run lint`)
3. `[3/4]` Frontend: Testes unitários e de componentes Vitest (`npm test`)
4. `[4/4]` Frontend: Checagem de tipos e Build Vite (`npm run build`)

#### 2. Execução Completa com Integração (Recomendada Pré-PR)

Adiciona a etapa de testes de integração com Testcontainers PostgreSQL (requer o serviço do Docker ativo):

```bash
./scripts/run-unit-tests.sh --with-integration
# ou de forma abreviada:
./scripts/run-unit-tests.sh -i
```

**Etapa adicional:**
5. `[5/5]` Backend: Testes de integração e isolamento multi-tenant (`./mvnw verify`)

#### 3. Menu de Ajuda

Para consultar os parâmetros e exemplos disponíveis:

```bash
./scripts/run-unit-tests.sh --help
```

#### 4. Automação com Git Hook (`pre-push`)

Para automatizar a execução da validação local e eliminar o risco de esquecimento antes de subir código para o repositório remoto, o repositório versiona o hook em `.githooks/pre-push`. Para ativá-lo:

```bash
git config core.hooksPath .githooks
```

Quando ativo, todo `git push` disparará automaticamente a validação rápida (`./scripts/run-unit-tests.sh`). Em caso de falha, o push é cancelado com código diferente de zero. Para contornar em casos excepcionais: `git push --no-verify`.

---

### Boas Práticas e Checklist Pré-PR

Antes de abrir qualquer Pull Request para a branch `main`:
1. Execute sempre `./scripts/run-unit-tests.sh` na raiz do projeto e certifique-se de que todas as etapas foram concluídas com sucesso.
2. Se o seu PR incluir alterações em entidades JPA, migrações Flyway, queries SQL ou filtros de segurança, execute obrigatoriamente `./scripts/run-unit-tests.sh --with-integration` para atestar a persistência real e o isolamento multi-tenant.
3. Não ignore alertas de linter ou erros de tipagem do TypeScript.
