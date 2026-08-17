---
workflowStatus: 'completed'
totalSteps: 5
stepsCompleted: ['step-01-detect-mode', 'step-02-load-context', 'step-03-risk-and-testability', 'step-04-coverage-plan', 'step-05-generate-output']
lastStep: 'step-05-generate-output'
nextStep: ''
lastSaved: '2026-08-17T12:00:00-03:00'
workflowType: 'testarch-test-design'
inputDocuments: 
  - '_bmad-output/planning-artifacts/prds/prd-projeto-aureus-docs-2026-08-02/prd.md'
  - '_bmad-output/planning-artifacts/architecture/architecture-projeto-aureus-docs-2026-08-14/ARCHITECTURE-SPINE.md'
---

# Test Design for Architecture: Aureus V1 (System-Level)

**Purpose:** Architectural concerns, testability gaps, and NFR requirements for review by Architecture/Dev teams. Serves as a contract between QA and Engineering on what must be addressed before test development begins.

**Date:** 2026-08-17
**Author:** TEA Master Test Architect
**Status:** Architecture Review Pending
**Project:** projeto-aureus
**PRD Reference:** `prd.md`
**ADR Reference:** `ARCHITECTURE-SPINE.md`

---

## Executive Summary

**Scope:** Planejamento arquitetural e de QA para o Aureus V1 (gestão de finanças com projeção de 24 meses).

**Business Context** (from PRD):
- **Problem:** Necessidade de visualizar a sobra retroativa acumulada de 24 meses para prever faltas e sobras financeiras.
- **GA Launch:** MVP local (uso pessoal).

**Architecture** (from ADR):
- **Key Decision 1:** Modular Monolith em Java Spring Boot + PostgreSQL.
- **Key Decision 2:** Autenticação OAuth 2.0 via Google com JWT HttpOnly cookie.
- **Key Decision 3:** Frontend React SPA com Zustand e React Query.

**Risk Summary:**
- **Total risks**: 5
- **High-priority (≥6)**: 2 risks requiring immediate mitigation
- **Test effort**: ~32-50 horas (1 QA / Test Architect full-time)

---

## Quick Guide

### 🚨 BLOCKERS - Team Must Decide (Can't Proceed Without)

**Pre-Implementation Critical Path** - These MUST be completed before QA can write integration tests:

1. **ASR-1: Auth Mock para E2E** - A arquitetura deve prever ou injetar um mock para o login do Google (ex: WireMock ou Cookie inject) (recommended owner: Backend Dev / Test Architect)

### ⚠️ HIGH PRIORITY - Team Should Validate (We Provide Recommendation, You Approve)

1. **TECH-1: Cálculos de Centavos** - A lógica de divisão de parcelas e alocação do resíduo (centavo sobrante) DEVE estar em uma classe utilitária altamente isolada e coberta por unit tests (recommended owner: Backend Dev).

### 📋 INFO ONLY - Solutions Provided (Review, No Decisions Needed)

1. **Test strategy**: API Tests para fluxos críticos de Auth e Isolamento; Playwright E2E para validação de UI (Consolidação e sincronia de abas).
2. **Tooling**: Playwright, JUnit (Backend), Playwright-Utils (API-Request, log).
3. **Tiered CI/CD**: PR (Functional, API, E2E), Nightly (N/A para V1, tudo no PR).
4. **Coverage**: ~11 test scenarios críticos priorizados de P0-P2.
5. **Quality gates**: P0 = 100% Pass Rate.

---

## For Architects and Devs - Open Topics 👷

### Risk Assessment

**Total risks identified**: 5 (2 high-priority score ≥6, 2 medium, 1 low)

#### High-Priority Risks (Score ≥6) - IMMEDIATE ATTENTION

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **TECH-1** | **TECH** | Erro de arredondamento nos cálculos de centavos fracionados gerando divergência na Consolidação. | 2 | 3 | **6** | Unit tests estritos na lógica de alocação de resíduo para garantir soma exata. | Backend Dev | Sprint 1 |
| **TECH-2** | **TECH** | Falhas intermitentes (flakiness) E2E devido ao login Google. | 3 | 2 | **6** | Implementar mock WireMock ou injeção de JWT de teste na pipeline E2E. | Test Architect | Pré-Automação |

#### Medium-Priority Risks (Score 3-5)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
| --- | --- | --- | --- | --- | --- | --- | --- |
| SEC-1 | SEC | Falha no filtro `usuario_id` expondo dados financeiros cross-tenant. | 1 | 3 | 3 | Validação mandatória no Repository e Testes de API limitando queries pelo tenant. | Backend Dev |
| PERF-1 | PERF | Lentidão no frontend ao computar 24 meses. | 2 | 2 | 4 | Teste limitador com massa extrema (500 itens/mês) e Playwright timings. | Frontend Dev |

#### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
| --- | --- | --- | --- | --- | --- | --- |
| OPS-1 | OPS | Escala indefinida para volumes não-pessoais. | 1 | 2 | 2 | Monitor |

---

### NFR Testability Requirements

| NFR Category | Threshold / Requirement | Current Design Support | Gap / Decision Needed | Planned Evidence |
| --- | --- | --- | --- | --- |
| Security | Isolamento Multi-Tenancy Lógico via JWT | Supported | N/A | Testes de API cruzados |
| Performance | Renderização responsiva dos 24 meses | Unknown | Faltam SLAs de latência para a Grid de Consolidação | Playwright Trace Timings |
| Reliability | Sincronia de Estado (React Query/Zustand) | Supported | N/A | Testes E2E multi-tab |
| Maintainability | Deploy Local via Docker Compose | Supported | N/A | Logs CI do Docker Build |

**Unknown thresholds:** SLAs de latência e volume de carga (transformados em PERF-1 e OPS-1).

### Testability Concerns and Architectural Gaps

**🚨 ACTIONABLE CONCERNS - Architecture Team Must Address**

#### 1. Blockers to Fast Feedback (WHAT WE NEED FROM ARCHITECTURE)

| Concern | Impact | What Architecture Must Provide | Owner | Timeline |
| --- | --- | --- | --- | --- |
| **Auth Mockability** | Impede automação E2E CI | Rota local bypass de auth ou mock OIDC configurado. | Backend Dev | Sprint 1 |
| **Test Data Generation** | E2E lento e frágil | Factory endpoints (POST /test/seed) ou DB scripts rodando via Flyway profile `test`. | Test Architect | Sprint 1 |

### Testability Assessment Summary

**📊 CURRENT STATE - FYI**

#### What Works Well
- ✅ Flyway Migration garante schema consistente e reprodutível nos testes.
- ✅ Modular Monolith isola domínios, permitindo testes de integração mais focados e limpos.
- ✅ Multi-tenancy lógico (`usuario_id`) propicia paralelismo completo (cada teste cria seu usuário único).

### Risk Mitigation Plans (High-Priority Risks ≥6)

#### TECH-1: Erro de arredondamento de centavos (Score: 6) - ALTO

**Mitigation Strategy:**
1. Criar classe `SplitAllocator` puramente matemática (stateless).
2. Desenvolver bateria intensiva de testes paramétricos JUnit cobrindo dízimas periódicas.
3. Não prosseguir para UI até as regras de centavos passarem nos testes unitários.

**Owner:** Backend Dev
**Status:** Planned

#### TECH-2: Flakiness por Login Google real (Score: 6) - ALTO

**Mitigation Strategy:**
1. Modificar o Spring Security local para aceitar um profile de `mockOidc`.
2. O profile deverá logar ou emitir JWT instantâneo sem depender da rede do Google.
3. Os testes Playwright E2E rodarão contra este profile.

**Owner:** Test Architect
**Status:** Planned

---
**End of Architecture Document**
