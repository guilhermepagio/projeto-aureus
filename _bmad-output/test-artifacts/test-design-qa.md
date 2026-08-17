---
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted: ['step-01-detect-mode', 'step-02-load-context', 'step-03-risk-and-testability', 'step-04-coverage-plan', 'step-05-generate-output']
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-17T12:08:00-03:00'
workflowType: 'testarch-test-design'
inputDocuments: 
  - '_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/prd.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-projeto-aureus-docs-2026-08-14/ARCHITECTURE-SPINE.md'
---

# Test Design for QA: Aureus V1 (System-Level)

**Purpose:** Test execution recipe for QA team. Defines what to test, how to test it, and what QA needs from other teams.

**Date:** 2026-08-17
**Author:** TEA Master Test Architect
**Status:** Draft
**Project:** projeto-aureus

**Related:** See Architecture doc (`test-design-architecture.md`) for testability concerns and architectural blockers.

---

## Executive Summary

**Scope:** Planejamento de cobertura e execução de testes (QA) cobrindo todo o MVP V1, abordando Autenticação, Contas, Despesas e o Painel de Consolidação de 24 meses.

**Risk Summary:**
- Total Risks: 5 (2 high-priority score ≥6, 2 medium, 1 low)
- Critical Categories: TECH (Cálculo de centavos, Dependência Google Auth).

**Coverage Summary:**
- P0 tests: ~4 (critical paths, auth, math)
- P1 tests: ~5 (important features, UI state)
- P2 tests: ~2 (edge cases, performance)
- P3 tests: ~0 
- **Total**: ~11 tests (~32-50 horas with 1 QA/Test Arch)

---

## Not in Scope

| Item | Reasoning | Mitigation |
| --- | --- | --- |
| **Integração Real com Bancos (Open Finance)** | O projeto baseia-se em lançamento manual de despesas. | Fora do PRD V1. |
| **App Mobile Nativo** | Foco primário na Web e Responsividade (PWA não cobrado estritamente). | Validação manual simples em Viewport Mobile no Playwright. |

---

## Dependencies & Test Blockers

**CRITICAL:** QA cannot proceed without these items from other teams.

### Backend/Architecture Dependencies (Pre-Implementation)

1. **Mock OIDC / Bypass Google Auth** - Backend Dev - Sprint 1
   - Configuração de um profile `test` ou injeção de Mock OIDC que permita login imediato localmente para uso do Playwright.
   - Sem isso, o Playwright será bloqueado por CAPTCHAs ou validações do Google Auth.

### QA Infrastructure Setup (Pre-Implementation)

1. **Test Data Factories** - QA
   - API helper tools (via Playwright-Utils) para gerar usuários dinâmicos, Categorias, Contas e lançar N Despesas Variáveis via requisições POST para popular o DB limpo.

2. **Test Environments** - QA
   - Local: `docker-compose up` subindo o Banco e a API; Frontend rodando em `localhost:5173`.
   - CI/CD: GitHub Actions (rodando o mesmo Compose).

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Score | QA Test Coverage |
| --- | --- | --- | --- | --- |
| **TECH-1** | TECH | Erro na divisão de centavos na Despesa Variável. | **6** | Garantido via bateria rigorosa de JUnit. Não sobrecarregar Playwright com isto. |
| **TECH-2** | TECH | Flakiness de UI devido ao login real do Google. | **6** | Teste API confirmando que o bypass route retorna 200 OK + JWT Válido. |

### Medium/Low-Priority Risks

| Risk ID | Category | Description | Score | QA Test Coverage |
| --- | --- | --- | --- | --- |
| SEC-1 | SEC | Vazamento cross-tenant de dados. | 3 | API Test P0 enviando Token de Usr A buscando ID de Despesa do Usr B (assert 403/404). |
| PERF-1 | PERF | Frontend lento ao carregar 24 meses. | 4 | Playwright injetará 500 despesas/mês e avaliará responsividade sem timeout explícito da UI. |
| OPS-1 | OPS | Ausência de meta de carga/throughput. | 2 | Fora de escopo de automação. Monitoramento. |

---

## NFR Test Coverage Plan

| NFR Category | Requirement / Threshold | Planned Validation | Tool / Level | Evidence Artifact | Priority |
| --- | --- | --- | --- | --- | --- |
| Security | Isolamento Multi-tenant JWT | Token Spoofing test (cross-tenant data access) | API Test | JUnit / Playwright API Report | P0 |
| Reliability | Multi-tab Sync no Frontend | Validar mês ativo persistente | E2E Playwright | Playwright HTML Report | P1 |
| Performance | Renderização da Grid | UI Rendering com grande volume de dados | E2E Playwright | Playwright Trace Viewer | P2 |
| Maintainability | Execução em contêineres | Verificar build da automação integrado ao Docker | CI | Logs CI | P1 |

---

## Entry Criteria

**QA testing cannot begin until ALL of the following are met:**
- [ ] Bypass de Auth (Mock OIDC ou Test Endpoint) implementado.
- [ ] Endpoints de CRUD de Contas e Categorias finalizados no backend.
- [ ] Test data factories funcionais.

## Exit Criteria

**Testing phase is complete when ALL of the following are met:**
- [ ] All P0 tests passing
- [ ] All P1 tests passing
- [ ] Backend com cobertura Unitária > 80% (foco em matemática de parcelas).

---

## Test Coverage Plan

### P0 (Critical)

**Criteria:** Blocks core functionality + High risk (≥6) + No workaround + Affects majority of users

| Test ID | Requirement | Test Level | Risk Link | Notes |
| --- | --- | --- | --- | --- |
| **P0-001** | Mock OIDC Login e injeção de Cookie | API/E2E | TECH-2 | Garantir login sem Google |
| **P0-002** | Acesso cross-tenant negado (403/404) | API | SEC-1 | Tentar ler dados do usuário Y com token X |
| **P0-003** | Cálculo exato das parcelas com resto | Unit | TECH-1 | Ex: R$ 10,00 em 3 parcelas = (3.34, 3.33, 3.33) |
| **P0-004** | Criação E2E via UI da Despesa Variável e Fixa | E2E | - | Caminho feliz principal |

---

### P1 (High)

**Criteria:** Important features + Medium risk (3-4) + Common workflows + Workaround exists but difficult

| Test ID | Requirement | Test Level | Risk Link | Notes |
| --- | --- | --- | --- | --- |
| **P1-001** | CRUD de Contas e Categorias | API | - | Validar consistência do DB |
| **P1-002** | Proteção ON DELETE RESTRICT (UI) | E2E | - | Tentar apagar categoria em uso |
| **P1-003** | Parcela única (à vista) | API | - | Validar fallback |
| **P1-004** | Consolidação: Exibir 0% quando Total=0 | Component | - | Validar UI logic |
| **P1-005** | Persistência do mês selecionado entre abas | E2E | - | Validar comportamento do Zustand |

---

### P2 (Medium)

**Criteria:** Secondary features + Low risk (1-2) + Edge cases + Regression prevention

| Test ID | Requirement | Test Level | Risk Link | Notes |
| --- | --- | --- | --- | --- |
| **P2-001** | Formulários de Despesa desabilitados sem dependências | E2E | - | UX: Sem conta/categoria não lança |
| **P2-002** | Grid 24 meses Performance Test | UI E2E | PERF-1 | Factory injeta 500 regs/mês |

---

## Execution Strategy

**Organized by TOOL TYPE:**

### Every PR: Playwright Tests (~3-5 min)

**All functional tests** (from any priority level):
- Todos E2E e API tests (Playwright) e Unit tests (JUnit).
- Total: 11 cenários principais. O volume de automação é reduzido, portanto não há segmentação complexa de pipelines. Roda 100% no PR.

---

## QA Effort Estimate

| Priority | Count | Effort Range | Notes |
| --- | --- | --- | --- |
| P0 | 4 | ~15-20 horas | Setup de Mock, Auth inject e Core E2E. |
| P1 | 5 | ~10-15 horas | API factories pesadas. |
| P2 | 2 | ~5-10 horas | Perf grid script. |
| **Total** | 11 | **~32-50 horas** | **1 QA engineer, part-time sprint focus** |
