---
runScope: 'system-level'
runKey: 'system'
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted: ['step-01-detect-mode', 'step-02-load-context', 'step-03-risk-and-testability', 'step-04-coverage-plan', 'step-05-generate-output']
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-17T12:09:00-03:00'
inputDocuments:
  - '_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/prd.md'
  - '_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/addendum.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-projeto-aureus-docs-2026-08-14/ARCHITECTURE-SPINE.md'
  - '_bmad-output/planning-artifacts/epics.md'
  - '.agents/skills/bmad-testarch-test-design/resources/knowledge/adr-quality-readiness-checklist.md'
  - '.agents/skills/bmad-testarch-test-design/resources/knowledge/nfr-criteria.md'
  - '.agents/skills/bmad-testarch-test-design/resources/knowledge/test-levels-framework.md'
  - '.agents/skills/bmad-testarch-test-design/resources/knowledge/risk-governance.md'
  - '.agents/skills/bmad-testarch-test-design/resources/knowledge/test-quality.md'
  - '.agents/skills/bmad-testarch-test-design/resources/knowledge/playwright-utils-mandate.md'
---

## Mode Confirmation
**Mode:** System-Level Mode
**Reasoning:** User intention was not explicit ("criar"), mas como o arquivo `sprint-status.yaml` está ausente e possuímos artefatos como PRD, Architecture e Epics, o modo System-Level é o padrão de prioridade de acordo com a detecção baseada em arquivos.
**Run Key Resolved:** `system`

## Context Loaded
**Configuração detectada:** Stack "fullstack" (backend + frontend).
**Artefatos do Projeto (System-Level):** PRD, Addendum, ARCHITECTURE-SPINE, Epics list.
**Informações extraídas:**
- Integrações: Google OAuth 2.0
- NFRs: Multitenancy lógico via JWT HttpOnly cookie, comunicação restrita via REST. 
- Tech Stack: Java Spring Boot (backend), React (SPA frontend), PostgreSQL, Flyway.
**Fragmentos de Conhecimento:** Core de qualidade, arquitetura, framework NFR e Playwright-Utils full profile.

## System-Level Testability Review

### 🚨 Testability Concerns
- **Controllability - Google OAuth 2.0:** Depender exclusivamente do login real do Google em testes end-to-end (E2E) trará grande instabilidade (bot detection, CAPTCHA, UI tracking). Será necessário um *mock* server (ex: WireMock) ou fluxo bypass para gerar JWT local.
- **Controllability - JWT em HttpOnly Cookie:** O uso de cookies HttpOnly significa que o frontend não pode manipular o token, exigindo que os testes E2E injetem o cookie diretamente no `BrowserContext` do Playwright ou dependam puramente da API de autenticação.
- **Observability - Logs por Usuário:** Para viabilizar testes paralelos com múltiplos tenants e diagnosticar falhas de isolamento, os logs do backend precisarão obrigatoriamente injetar o `usuario_id` usando MDC (Mapped Diagnostic Context).

### ✅ Testability Assessment Summary
- **Reliability (Isolation & Parallel Safety):** A restrição arquitetural do Multi-Tenancy lógico (`usuario_id` obrigatório e isolamento de dados) garante que diferentes testes automatizados executando em paralelo não colidam em massa de dados, desde que cada teste crie e utilize seu próprio usuário gerado dinamicamente.
- **Observability (State Management):** O uso de Zustand no frontend e respostas REST via React Query permite a criação de asserções assíncronas consistentes (ex: aguardando indicadores de *loading* do React Query finalizarem), aumentando a robustez das automações e prevenindo *race conditions*.
- **Controllability (Database State):** O uso do Flyway (migrations versionadas) garante reprodutibilidade do *schema* durante o teardown/setup local e de CI para os testes.

### Architecturally Significant Requirements (ASRs)
- **[ACTIONABLE] ASR-1:** Autenticação OIDC Google + JWT HttpOnly Cookie. Requer estratégia de bypass de auth no nível do Playwright.
- **[FYI] ASR-2:** Multi-Tenancy Lógico (`usuario_id`). Permite paralelismo limpo de testes; obriga injeção correta de tenant-id.
- **[FYI] ASR-3:** Proteção `ON DELETE RESTRICT` (Contas e Categorias). Exige ordem rígida no teardown de massa de testes (excluir Despesas antes das Categorias/Contas).

## NFR Planning Assessment

- **[NFR1] Ambiente Local (Docker/Java):** Limite de recursos local para rodar backend, DB e testes E2E.
  - **Evidência Planejada:** Relatórios do pipeline de Integração Contínua (CI) verificando o build via Docker Compose.
- **[NFR2] Isolamento de Dados (Security):** 
  - **Evidência Planejada:** Testes de integração backend enviando token de um usuário e tentando acessar ID de outro. Resposta esperada HTTP 403/404.
- **[NFR3 / NFR4] Resposta Assíncrona e Sincronização de Estado (Reliability/UX):** 
  - **Evidência Planejada:** Testes Playwright avaliando persistência do "mês ativo" ao mudar de aba sem disparar `page.reload()`.
- **[NFR-PERF] Performance / Response Time:** **[UNKNOWN]**. Não há SLAs no PRD para os blocos da Consolidação de 24 meses. Convertido no risco **PERF-1**.
- **[NFR-SCA] Escalabilidade (Concurrent Users):** **[UNKNOWN]**. Volume simultâneo de requisições não especificado. Convertido no risco **OPS-1**.

## Risk Assessment Matrix

| Categoria | Risco | Prob | Impacto | Score | Mitigação | Owner | Timeline |
|-----------|-------|------|---------|-------|-----------|-------|----------|
| **SEC-1** | Falha no filtro `usuario_id` expondo dados financeiros cross-tenant. | 1 | 3 | **3** | Incluir validação mandatória de ownership em nível de DAO/Repository com testes de integração cobrindo os limites do tenant. | Backend Dev | Sprint 1 |
| **TECH-1** | Erro de arredondamento nos cálculos de centavos fracionados (ex: parcelas), gerando divergência na Consolidação. | 2 | 3 | **6 (ALTO)** | Unit tests abrangentes na lógica de alocação de resíduo para garantir soma estrita entre parcelas e total. | Backend Dev | Sprint 1 |
| **TECH-2** | Falhas intermitentes (flakiness) em E2E devido à dependência do login Google real (captchas/rate limit). | 3 | 2 | **6 (ALTO)** | Implementar mock de OIDC via WireMock ou via injeção direta de Cookie para ambiente de testes. | Test Architect | Pré-Automação |
| **PERF-1** | Lentidão no frontend ao computar os 24 meses na Grade de Consolidação na mesma View. | 2 | 2 | **4** | Criar métricas ou testes limitadores com massas de dados extremas (ex: 500 registros/mês) no Playwright para testar responsividade. | Frontend Dev | Sprint 2 |
| **OPS-1** | Indefinição sobre escalabilidade (ausência de limite de RPS e Load). | 1 | 2 | **2** | Assumir perfil "uso individual/portfólio" e definir métrica baseline de 100 req/s se a aplicação crescer. | Product/Dev | FYI |

## Summary of Risk Findings
Os riscos mais expressivos enfrentados pela V1 do Aureus são do tipo Técnico (**TECH-1** e **TECH-2**). O risco **TECH-2** (dependência do Google Auth nos testes E2E) é o maior ofensor de testabilidade, devendo ser mitigado por bypass local o mais rápido possível para garantir a estabilidade do Playwright. O **TECH-1** (matemática das parcelas) requer extrema cobertura unitária para validar a filosofia core da ferramenta (distribuição exata de dinheiro). Riscos arquiteturais como vazamento de tenant (**SEC-1**) possuem probabilidade baixa graças ao Flyway e JPA estruturado, mas o impacto desastroso exige asserções *negative-testing* em nível de integração de API.

## Coverage Matrix

### Epic 1: Autenticação e Navegação Segura
| Cenário | Test Level | Priority | NFR/Risco Mapeado |
|---------|------------|----------|-------------------|
| Mock OIDC Login e injeção de JWT Cookie | API / E2E | P0 | ASR-1, TECH-2 |
| Acesso cross-tenant negado (403/404) | API | P0 | SEC-1, NFR2 |
| Logout invalida sessão e limpa state | E2E | P1 | - |

### Epic 2: Contas e Categorias
| Cenário | Test Level | Priority | NFR/Risco Mapeado |
|---------|------------|----------|-------------------|
| CRUD Completo Conta/Categoria | API | P1 | - |
| UI de bloqueio ao tentar deletar Conta com vínculo | E2E | P1 | ASR-3 |
| Formulários desabilitados se faltam dependências | Component | P2 | - |

### Epic 3: Lançamentos Financeiros
| Cenário | Test Level | Priority | NFR/Risco Mapeado |
|---------|------------|----------|-------------------|
| Despesa Variável: Divisão e alocação exata de centavos na primeira parcela | Unit | P0 | TECH-1 |
| Criação de Despesa Fixa e Variável via formulário UI | E2E | P0 | - |
| Fluxo de Parcela Única (à vista) reflete 1 parcela com mesmo valor total | API | P1 | - |
| Sincronização automática via React Query ao mutar dados | E2E | P0 | NFR4 |

### Epic 4: Consolidação Mensal
| Cenário | Test Level | Priority | NFR/Risco Mapeado |
|---------|------------|----------|-------------------|
| Cálculo da Sobra do Mês e Sobra Retroativa Acumulada | Unit / API | P0 | - |
| Exibição de 0% para Categorias quando Total de Despesas = 0 | Component | P1 | - |
| Persistência do mês selecionado entre abas (Zustand) | E2E | P1 | NFR3 |
| Grid de 24 meses não quebra com 500 registros/mês | UI Perf | P2 | PERF-1 |

## NFR Coverage and Evidence Plan

| NFR / Risco | Validation Scenario | Level / Tool | Expected Evidence Artifact |
|-------------|---------------------|--------------|----------------------------|
| **NFR1** (Deploy Local) | Build & Run de containers via Docker | CI | Logs do job de build do CI (Pass/Fail) |
| **NFR2** (Multi-Tenancy) | Envio de token modificado tentando burlar o `usuario_id` | API Test | API Test Report (`/test-results`) |
| **NFR3 / NFR4** (UX/Reliability) | Navegação via abas e validação visual de *loading* skeletons sem `reload` | E2E Playwright | Playwright HTML Report |
| **PERF-1** (Grid 24M Loading) | Inserção de 500 registros via factory e assert do tempo de render | E2E Playwright | Trace viewer / Timing logs do Playwright |
| **OPS-1** (Concurrent Users) | Risco aceito para a V1; sem automação necessária | N/A | N/A |

## Execution Strategy

A estratégia baseada no modelo **PR / Nightly**:

*   **PR (Pull Request):**
    *   Testes Unitários (Jest/JUnit) rodando em cada commit. (Tempo estimado: < 1 min)
    *   Testes de API Rest (Pact/Playwright/Spring) e Componentes (RTL). (Tempo estimado: < 3 min)
    *   Testes E2E core (Playwright) rodando os cenários P0 e P1. (Tempo estimado: < 10 min)
*   **Nightly / Weekly:**
    *   Devido à simplicidade da V1, todos os testes rodarão no PR. O cenário de performance (PERF-1) poderá ser isolado em Nightly caso ultrapasse 5 minutos.

## Resource Estimates

Estimativas de esforço de automação para a V1, distribuídas pelas prioridades:

*   **P0 (Críticos e Riscos Altos):** ~15–20 horas (Engloba Mock de Auth, Unit tests matemáticos e Core E2E).
*   **P1 (Caminhos Críticos Médios):** ~10–15 horas.
*   **P2 (Fluxos Secundários):** ~5–10 horas.
*   **P3 (Nice-to-have):** ~2–5 horas (Não mapeado no momento).
*   **Total Estimado:** ~32–50 horas.

## Quality Gates

Critérios mínimos para liberar o produto (Go/No-Go):

*   **Pass Rate P0:** 100% (Qualquer quebra impede merge/deploy).
*   **Pass Rate P1:** ≥ 95% (Falhas toleráveis apenas se houver mitigação imediata catalogada).
*   **High-Risk Mitigations:** TECH-1 (Cálculo de parcelas) e TECH-2 (Mock Auth) **devem** ser resolvidos e automatizados antes do release.
*   **Cobertura:** ≥ 80% do código testado nas camadas Backend e Componentes.
*   **NFR Validation:** As evidências (Relatórios Playwright, CI Logs) deverão ser apresentadas na assessment final.

## Completion Report

- **Mode used**: System-Level Mode (`sequential`)
- **Output file paths**:
  - `_bmad-output/test-artifacts/test-design-architecture.md`
  - `_bmad-output/test-artifacts/test-design-qa.md`
  - `_bmad-output/test-artifacts/test-design/projeto-aureus-handoff.md`
- **Key risks and gate thresholds**:
  - **TECH-1** e **TECH-2** identificados como maiores bloqueios/riscos técnicos de V1 (pontuação de 6).
  - Gate 100% Pass Rate para P0 obrigatório.
- **Open assumptions**:
  - Faltam definições claras de throughput (RPS), gerando a premissa de um uso single-player inicial em docker-compose.
