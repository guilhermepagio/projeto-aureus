---
title: 'TEA Test Design → BMAD Handoff Document'
version: '1.0'
workflowType: 'testarch-test-design-handoff'
inputDocuments: 
  - '_bmad-output/test-artifacts/test-design-architecture.md'
  - '_bmad-output/test-artifacts/test-design-qa.md'
sourceWorkflow: 'testarch-test-design'
generatedBy: 'TEA Master Test Architect'
generatedAt: '2026-08-17T12:08:00-03:00'
projectName: 'projeto-aureus'
---

# TEA → BMAD Integration Handoff

## Purpose

This document bridges TEA's test design outputs with BMAD's epic/story decomposition workflow (`create-epics-and-stories`). It provides structured integration guidance so that quality requirements, risk assessments, and test strategies flow into implementation planning.

## TEA Artifacts Inventory

| Artifact             | Path                      | BMAD Integration Point                               |
| -------------------- | ------------------------- | ---------------------------------------------------- |
| Test Design Document | `_bmad-output/test-artifacts/test-design-architecture.md` | Epic quality requirements, story acceptance criteria |
| Test Design QA       | `_bmad-output/test-artifacts/test-design-qa.md`           | Executable test coverage tracking                    |
| Risk Assessment      | `_bmad-output/test-artifacts/test-design-architecture.md` | Epic risk classification, story priority             |
| Coverage Strategy    | `_bmad-output/test-artifacts/test-design-qa.md`           | Story test requirements                              |

## Epic-Level Integration Guidance

### Risk References

1. **[TECH-1] Erro de arredondamento nos cálculos (Score: 6)**: O epic responsável pelas Despesas Variáveis deve carregar este risco como impeditivo de release. O Backend deve prover garantia matemática.
2. **[TECH-2] Falhas intermitentes E2E com Google Auth (Score: 6)**: O epic de Infraestrutura/Auth deve conter a tarefa de habilitar o mock OIDC/Bypass para estabilidade do Playwright.

### Quality Gates

- **Auth Mock:** Nenhum teste Playwright deve tentar usar a interface real do Google Auth em CI.
- **Pass Rate:** 100% obrigatório em cenários críticos (P0).
- **Security:** O isolamento cross-tenant (`usuario_id`) deve ser testado em nível de integração antes da liberação.

## Story-Level Integration Guidance

### P0/P1 Test Scenarios → Story Acceptance Criteria

- **Story: Login de Usuário (Auth)**
  - *AC obrigatório:* Deve existir um bypass de auth para automação que evite o fluxo real do Google.
- **Story: Criação de Despesa Variável**
  - *AC obrigatório:* A soma exata de todas as parcelas geradas deve ser rigorosamente igual ao valor total da Despesa original, alocando a dízima/centavo na primeira parcela.
- **Story: Arquitetura Multi-Tenant**
  - *AC obrigatório:* Acesso a recursos de um Tenant 'A' usando token do Tenant 'B' deve retornar HTTP 403 (ou 404 seguro).
- **Story: Painel de Consolidação**
  - *AC obrigatório:* A UI deve persistir a visualização do mês caso o usuário navegue entre abas do dashboard, usando estado local (Zustand).

### Data-TestId Requirements

Para as histórias de Frontend (UI), incluir obrigatoriamente os seguintes atributos `data-testid` nos componentes:
- Botão/Flow Login: `[data-testid="login-button"]`
- Painel de 24 meses: `[data-testid="consolidation-grid"]`, `[data-testid="month-card-{YYYY-MM}"]`
- Formulários de Despesa: `[data-testid="form-despesa-fixa"]`, `[data-testid="form-despesa-variavel"]`
- Itens de lista e ações de exclusão devem ter testids com o ID da entidade ex: `[data-testid="delete-btn-{id}"]`

## Risk-to-Story Mapping

| Risk ID | Category | P×I | Recommended Story/Epic | Test Level |
| ------- | -------- | --- | ---------------------- | ---------- |
| TECH-1  | TECH     | 6   | Epic 3 (Lançamentos / Despesa Variável) | Unit |
| TECH-2  | TECH     | 6   | Epic 1 (Autenticação) | API / Infra |
| SEC-1   | SEC      | 3   | Epic 1 (Security/Auth) | API |
| PERF-1  | PERF     | 4   | Epic 4 (Consolidação UI) | E2E Playwright |

## Recommended BMAD → TEA Workflow Sequence

1. **TEA Test Design** (`TD`) → produces this handoff document
2. **BMAD Create Epics & Stories** → consumes this handoff, embeds quality requirements
3. **TEA ATDD** (`AT`) → generates acceptance tests per story
4. **BMAD Implementation** → developers implement with test-first guidance
5. **TEA Automate** (`TA`) → generates full test suite
6. **TEA Trace** (`TR`) → validates coverage completeness

## Phase Transition Quality Gates

| From Phase          | To Phase            | Gate Criteria                                          |
| ------------------- | ------------------- | ------------------------------------------------------ |
| Test Design         | Epic/Story Creation | All P0 risks have mitigation strategy                  |
| Epic/Story Creation | ATDD                | Stories have acceptance criteria from test design      |
| ATDD                | Implementation      | Failing acceptance tests exist for all P0/P1 scenarios |
| Implementation      | Test Automation     | All acceptance tests pass                              |
| Test Automation     | Release             | Trace matrix shows ≥80% coverage of P0/P1 requirements |
