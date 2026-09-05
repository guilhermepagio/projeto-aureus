---
title: 'Epic 3 Story 2: Lançamentos Variáveis e Parcelados'
type: 'feature'
created: '2026-08-21'
status: 'done'
baseline_commit: '42305863a7d95cc8bdf6a1a6dfb9b90bc4fd975a'
review_loop_iteration: 0
context: 
  - '{project-root}/_bmad-output/implementation-artifacts/epic-3-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Atualmente, o sistema só suporta lançamentos fixos (Story 3.1). Não é possível registrar despesas ou receitas variáveis parceladas com um horizonte de tempo finito, limitando a visão real das obrigações ou ganhos futuros.

**Approach:** Criar as entidades `DespesaVariavel` e `ReceitaVariavel`. Diferente dos lançamentos fixos, as variáveis possuem uma quantidade definida de parcelas. O registro será único por lançamento (não haverá persistência de parcelas filhas no banco de dados), armazenando a `dataInicio` e a `dataFim` (calculada dinamicamente com base na quantidade de parcelas). No frontend, o usuário informará o "Valor da Parcela" e o "Nº de Parcelas", e o sistema exibirá o Valor Total calculado.

## Boundaries & Constraints

**Always:**
- Entidades estendem `TenantAwareEntity`.
- FKs para `contas` e `categorias` com `ON DELETE RESTRICT`.
- Registro Único: Não deve ser criada tabela ou entidade separada para as parcelas. Cada lançamento é uma única linha na tabela `despesas_variaveis` ou `receitas_variaveis`.
- Lógica de Datas: `dataInicio` é o mês de competência (Primeira Parcela). `dataFim` é calculada como `dataInicio + (Nº Parcelas - 1) meses`. Ex: 01/01/2026 com 3 parcelas -> dataFim = 01/03/2026.
- A entidade `DespesaVariavel` deve possuir os campos extras `localCompra` (texto, opcional) e `dataCompra` (data, opcional). `ReceitaVariavel` não possui esses campos.
- O Valor Total não precisa ser salvo no banco, sendo a multiplicação de `valorParcela * quantidadeParcelas`, mas pode ser devolvido via DTO ou calculado no front.

**Ask First:**
- Qualquer nova dependência externa (pom.xml ou package.json).

**Never:**
- Não criar entidades filhas de parcelas (`ParcelaDespesaVariavel`).
- Não implementar lógica de penny rounding (divisão de centavos), pois a entrada primária é o Valor da Parcela, e não o Valor Total.
- Não implementar lógicas globais de "Filtro de Mês" do Zustand nesta Story (é escopo da Story 3.4).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Criação de Despesa Variavel | valorParcela = 50.00, parcelas = 3, dataInicio = 2026-01-01 | Salva com dataFim = 2026-03-01. Total calculado = 150.00 | N/A |
| Campos exclusivos da Despesa | Preenche localCompra e dataCompra | Dados são persistidos junto com a Despesa | N/A |

</frozen-after-approval>

## Code Map

- `backend/.../domain/DespesaVariavel.java` -- Nova Entidade principal
- `backend/.../domain/ReceitaVariavel.java` -- Nova Entidade principal
- `backend/.../repository/DespesaVariavelRepository.java`
- `backend/.../repository/ReceitaVariavelRepository.java`
- `backend/.../controller/DespesaVariavelController.java`
- `backend/.../controller/ReceitaVariavelController.java`
- `frontend/src/hooks/useDespesasVariaveis.ts`
- `frontend/src/hooks/useReceitasVariaveis.ts`
- `frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx`
- `frontend/src/pages/DespesasVariaveis/components/DespesaVariavelFormModal.tsx`
- `frontend/src/pages/ReceitasVariaveis/ReceitasVariaveisPage.tsx`
- `frontend/src/pages/ReceitasVariaveis/components/ReceitaVariavelFormModal.tsx`
- `frontend/src/App.tsx` -- Adicionar rotas.

## Tasks & Acceptance

**Execution:**
- [x] `backend/.../domain/DespesaVariavel.java` -- Criar entidade. Campos na ordem de formulário: descricao, localCompra (String), dataCompra (LocalDate), valorParcela, quantidadeParcelas, dataInicio (LocalDate - referente à Primeira Parcela), dataFim (LocalDate - referente à Última Parcela), e FKs de categoria e conta, observacoes. Extends TenantAwareEntity.
- [x] `backend/.../domain/ReceitaVariavel.java` -- Criar entidade. Campos na ordem de formulário: descricao, valorParcela, quantidadeParcelas, dataInicio (LocalDate), dataFim (LocalDate), e FKs de categoria e conta, observacoes. Extends TenantAwareEntity.
- [x] `backend/.../repository/*` -- Repositories básicos.
- [x] `backend/.../controller/*` -- Controllers REST CRUD. No POST/PUT, garantir que a `dataFim` seja calculada e setada antes de salvar (ex: `dataInicio.plusMonths(quantidadeParcelas - 1)`).
- [x] `frontend/src/hooks/*` -- React Query hooks.
- [x] `frontend/src/pages/.../components/DespesaVariavelFormModal.tsx` -- Formulário com ordem estrita: Descrição, Local Compra, Data Compra, Valor Parcela, Nº Parcelas, Valor Total (Calculation Preview), Primeira Parcela, Última Parcela (Preview), Categoria, Conta, Observações.
- [x] `frontend/src/pages/.../components/ReceitaVariavelFormModal.tsx` -- Formulário com ordem estrita: Descrição, Valor Parcela, Nº Parcelas, Valor Total (Calculation Preview), Primeira Parcela, Última Parcela (Preview), Categoria, Conta, Observações.
- [x] `frontend/src/pages/.../*Page.tsx` -- Páginas de lista integradas.
- [x] `frontend/src/App.tsx` -- Rotas no React Router.

**Acceptance Criteria:**
- Given o formulário de Despesa Variável, when o usuário digita "Valor da Parcela" = 100 e "Nº Parcelas" = 3 com Data de Início em "Jan/2026", then o Calculation Preview exibe "Valor Total: 300" e o backend persiste apenas UM registro com dataInicio = 2026-01-01 e dataFim = 2026-03-01.
- Given o formulário de Despesa Variável, then os campos opcionais "Local da Compra" e "Data da Compra" devem estar disponíveis para preenchimento.
- Given o formulário de Receita Variável, then ele deve funcionar de forma semelhante, porém sem os campos "Local da Compra" e "Data da Compra".

## Spec Change Log

## Design Notes

- Como as parcelas não são entidades separadas, toda a lógica de projeção na consolidação (Epic 4) verificará simplesmente se o mês da coluna visualizada está entre a `dataInicio` e a `dataFim` da despesa/receita variável, o que elimina gargalos de performance e excesso de linhas no banco de dados.

## Verification

**Commands:**
- `cd backend && ./mvnw compile` -- expected: BUILD SUCCESS
- `cd frontend && npx tsc --noEmit` -- expected: sem erros de tipagem

## Suggested Review Order

**Backend Core (API & Domínio)**

- Nova entidade com atributos obrigatórios e lógica isolada sem parcelamento
  [`DespesaVariavel.java:32`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaVariavel.java#L32)

- Cálculo automático de dataFim acoplado diretamente no controller de criação e atualização
  [`DespesaVariavelController.java:51`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaVariavelController.java#L51)

**Frontend Data Layer**

- Gerenciamento de fetch e mutations para os lançamentos variáveis com invalidação de cache
  [`useDespesasVariaveis.ts:60`](../../frontend/src/hooks/useDespesasVariaveis.ts#L60)

**Frontend View Layer (UI & Formulários)**

- Formulário mantendo ordem estrita com Preview dinâmico para Valor Total e Última Parcela
  [`DespesaVariavelFormModal.tsx:45`](../../frontend/src/pages/DespesasVariaveis/components/DespesaVariavelFormModal.tsx#L45)

- Página de listagem com componentes padrões e integração de modais de exclusão
  [`DespesasVariaveisPage.tsx:34`](../../frontend/src/pages/DespesasVariaveis/DespesasVariaveisPage.tsx#L34)

**Integração de Rotas**

- Inserção de rotas da aplicação para navegação completa das novas páginas
  [`App.tsx:85`](../../frontend/src/App.tsx#L85)

### Review Findings
- [x] [Review][Patch] Incorrect Temporal Competence Handling and Date Rollover Bug [DespesaVariavelFormModal.tsx]
- [x] [Review][Patch] Incorrect Table Alignment for Numeric/Monetary Values [DespesasVariaveisPage.tsx]
- [x] [Review][Patch] Missing safe currency arithmetic in frontend [DespesaVariavelFormModal.tsx]
- [x] [Review][Patch] Stale error message shown for different record in DeleteConfirmModal [DeleteConfirmModal.tsx]
- [x] [Review][Defer] Missing automated verification tests (dataFim, relational errors, API parsing) [backend/frontend] — deferred, pre-existing
- [x] [Review][Defer] Missing DTOs, Service Layer, and Global Exception Handler [backend] — deferred, pre-existing
- [x] [Review][Patch] Inconsistent Theme/Styling for Expense Page Header [DespesasVariaveisPage.tsx]
- [x] [Review][Patch] dataInicio Day drift in calculation [DespesaVariavelController.java]
- [x] [Review][Patch] Missing saveAndFlush() in controllers [DespesaVariavelController.java]
- [x] [Review][Patch] @Max(9999999) used on valorParcela instead of @DecimalMax [DespesaVariavel.java]
- [x] [Review][Patch] Missing maxLength={100} on descricao and localCompra inputs [DespesaVariavelFormModal.tsx]
- [x] [Review][Defer] Fetcher swallows non-JSON 500 errors [useDespesasVariaveis.ts] — deferred, pre-existing
