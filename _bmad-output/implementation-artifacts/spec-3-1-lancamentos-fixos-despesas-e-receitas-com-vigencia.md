---
title: 'Epic 3 Story 1: Lançamentos Fixos (Despesas e Receitas) com Vigência'
type: 'feature'
created: '2026-08-20'
status: 'done'
baseline_commit: 'bfbc17cef1a49da75dc8d1252489f45b58ba2a60'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-3-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O sistema não possui cadastro de receitas e despesas fixas recorrentes. Sem isso, valores mensais contínuos (aluguel, salário, internet) não são registrados nem projetados na grade de consolidação.

**Approach:** Criar entidades `DespesaFixa` e `ReceitaFixa` no backend com FK para Conta e Categoria, expor endpoints REST CRUD (`/api/despesas-fixas`, `/api/receitas-fixas`) e implementar as páginas frontend completas (listagem, formulário Modal/Bottom Sheet, exclusão com confirmação) substituindo os placeholders atuais nas rotas `/despesas-fixas` e `/receitas-fixas`.

## Boundaries & Constraints

**Always:**
- Entidades estendem `TenantAwareEntity` para isolamento multi-tenant automático.
- FKs para `contas` e `categorias` com `ON DELETE RESTRICT`.
- Valores monetários como `BigDecimal` (backend) com `@Column(precision = 15, scale = 2)`.
- Competência de início como `YearMonth` (persistida como `date` no PostgreSQL, primeiro dia do mês).
- Seguir padrão de controllers existentes (injeção direta de repositório, sem service layer).
- Frontend segue padrão de hooks existentes (`useContas`/`useCategorias`): fetch + CSRF + React Query + toast.
- Formulários abrem em Modal (Desktop) / Bottom Sheet (Mobile) com focus trapping.

**Ask First:**
- Qualquer nova dependência no `pom.xml` ou `package.json`.
- Mudanças no schema de autenticação/segurança.

**Never:**
- Não implementar parcelamento, cálculo de parcelas ou lançamentos variáveis (escopo da Story 3.2/3.3).
- Não implementar filtro por mês ou sincronização global de mês (escopo da Story 3.4).
- Não criar camada de service — manter padrão atual de controller → repository.
- Não usar `hibernate.ddl-auto` para migrações — usar Hibernate auto-update como o projeto faz hoje.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Criar despesa fixa válida | Descrição, Valor > 0, Conta, Categoria, Data Início | Registro salvo, toast sucesso, lista atualizada | N/A |
| Criar sem descrição | Descrição vazia | Validação inline: "A descrição é obrigatória" | Campo fica com borda vermelha |
| Criar sem conta selecionada | Conta = null | Validação: "Selecione uma conta" | Submissão bloqueada |
| Criar sem categoria selecionada | Categoria = null | Validação: "Selecione uma categoria" | Submissão bloqueada |
| Valor zero ou negativo | Valor <= 0 | Validação backend: "O valor deve ser maior que zero" | Toast de erro |
| Editar valor de item fixo | Novo valor informado | Registro atualizado in-place, toast sucesso | N/A |
| Excluir item fixo | Clica excluir → confirma | Registro removido, toast sucesso, lista atualizada | N/A |
| Excluir item — cancela | Clica excluir → cancela no modal | Nenhuma ação | N/A |
| Lista vazia | Nenhum registro para o tipo | Empty state com ícone + CTA "Adicionar" | N/A |

</frozen-after-approval>

## Code Map

- `backend/.../domain/DespesaFixa.java` -- Nova entidade. Extends `TenantAwareEntity`. Campos: id, descricao, valor (BigDecimal), dataInicio (LocalDate), observacoes. FK: conta_id → contas, categoria_id → categorias.
- `backend/.../domain/ReceitaFixa.java` -- Nova entidade análoga à DespesaFixa para receitas.
- `backend/.../repository/DespesaFixaRepository.java` -- `JpaRepository<DespesaFixa, Long>`.
- `backend/.../repository/ReceitaFixaRepository.java` -- `JpaRepository<ReceitaFixa, Long>`.
- `backend/.../controller/DespesaFixaController.java` -- CRUD REST: GET, POST, PUT, DELETE em `/api/despesas-fixas`. Padrão de `ContaController`.
- `backend/.../controller/ReceitaFixaController.java` -- CRUD REST análogo em `/api/receitas-fixas`.
- `frontend/src/hooks/useDespesasFixas.ts` -- Hook React Query: fetch, create, update, delete com CSRF e toast. Padrão de `useCategorias.ts`.
- `frontend/src/hooks/useReceitasFixas.ts` -- Hook análogo para receitas fixas.
- `frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx` -- Página de listagem. Substitui placeholder em `App.tsx`. Padrão de `CategoriasPage.tsx`.
- `frontend/src/pages/DespesasFixas/components/DespesaFixaFormModal.tsx` -- Formulário modal com campos: Descrição, Valor, Conta (select), Categoria (select), Data Início (month picker), Observações.
- `frontend/src/pages/DespesasFixas/components/DeleteConfirmModal.tsx` -- Confirmação de exclusão.
- `frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx` -- Página de listagem análoga.
- `frontend/src/pages/ReceitasFixas/components/ReceitaFixaFormModal.tsx` -- Formulário modal análogo.
- `frontend/src/pages/ReceitasFixas/components/DeleteConfirmModal.tsx` -- Confirmação de exclusão.
- `frontend/src/App.tsx` L67-68 -- Substituir placeholders `DespesasFixas` e `ReceitasFixas` por imports reais.

## Tasks & Acceptance

**Execution:**
- [x] `backend/.../domain/DespesaFixa.java` -- Criar entidade -- Extends TenantAwareEntity, campos id/descricao/valor/dataInicio/observacoes com FKs @ManyToOne para Conta e Categoria, validações @NotBlank/@NotNull/@Positive/@Size.
- [x] `backend/.../domain/ReceitaFixa.java` -- Criar entidade -- Estrutura idêntica à DespesaFixa para receitas fixas.
- [x] `backend/.../repository/DespesaFixaRepository.java` -- Criar repository -- JpaRepository simples.
- [x] `backend/.../repository/ReceitaFixaRepository.java` -- Criar repository -- JpaRepository simples.
- [x] `backend/.../controller/DespesaFixaController.java` -- Criar controller CRUD -- GET/POST/PUT/DELETE seguindo padrão de ContaController com @Valid.
- [x] `backend/.../controller/ReceitaFixaController.java` -- Criar controller CRUD -- Estrutura análoga ao DespesaFixaController.
- [x] `frontend/src/hooks/useDespesasFixas.ts` -- Criar hook -- Interface DespesaFixa, fetchers com CSRF, hooks useQuery/useMutation com invalidação e toasts.
- [x] `frontend/src/hooks/useReceitasFixas.ts` -- Criar hook -- Estrutura análoga para receitas fixas.
- [x] `frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx` -- Criar página -- Lista com empty state, FAB, integração com form modal e delete modal.
- [x] `frontend/src/pages/DespesasFixas/components/DespesaFixaFormModal.tsx` -- Criar formulário -- Campos: descrição, valor (input numérico), conta (select populado por useContas), categoria (select por useCategorias), dataInicio (input month), observações. Validação inline.
- [x] `frontend/src/pages/DespesasFixas/components/DeleteConfirmModal.tsx` -- Criar confirmação -- Modal de exclusão seguindo padrão existente.
- [x] `frontend/src/pages/ReceitasFixas/ReceitasFixasPage.tsx` -- Criar página -- Estrutura análoga a DespesasFixasPage.
- [x] `frontend/src/pages/ReceitasFixas/components/ReceitaFixaFormModal.tsx` -- Criar formulário -- Estrutura análoga.
- [x] `frontend/src/pages/ReceitasFixas/components/DeleteConfirmModal.tsx` -- Criar confirmação -- Estrutura análoga.
- [x] `frontend/src/App.tsx` -- Atualizar rotas -- Substituir placeholders por componentes reais importados.

**Acceptance Criteria:**
- Given usuário autenticado com Contas e Categorias cadastradas, when acessa `/despesas-fixas` e preenche o formulário com dados válidos, then a despesa fixa é salva e aparece na lista.
- Given usuário autenticado, when edita o valor de uma despesa fixa existente, then o registro é atualizado in-place e a lista reflete a mudança.
- Given usuário autenticado, when exclui uma despesa fixa e confirma no modal, then o registro é removido e um toast de sucesso é exibido.
- Given nenhuma despesa fixa cadastrada, when acessa `/despesas-fixas`, then um empty state com CTA é exibido.
- Given mesmos cenários acima, when aplicados a `/receitas-fixas`, then o comportamento é idêntico para receitas fixas.
- Given dados inválidos (descrição vazia, valor <= 0, conta/categoria não selecionada), when tenta salvar, then validação inline bloqueia a submissão com mensagens específicas.

### Review Findings

- [x] [Review][Patch] Resposta de erro não é JSON ("Erro de integridade") — Backend retorna string simples, quebrando o `await response.json()` no frontend.
- [x] [Review][Patch] Bypass do Try-Catch no PUT — `atualizar` usa `save()` sem `@Transactional`, fazendo com que a exceção de integridade escape da captura.
- [x] [Review][Patch] Vulnerabilidade de ID Hijacking no POST — `criar` não zera o `id` da entidade, permitindo overwrite indevido se o cliente enviar um ID.
- [x] [Review][Patch] Risco de LazyInitializationException — Entidades retornadas com relacionamentos `FetchType.LAZY` causarão erro na serialização JSON.
- [x] [Review][Patch] Mensagem de exclusão hardcoded no Frontend — Resposta de erro 400 do backend é ignorada em favor de uma mensagem fixa.
- [x] [Review][Patch] Falta de ordenação padrão na Listagem — `findAll()` retorna dados em ordem arbitrária; falta um `Sort.by()`.
- [x] [Review][Defer] Falta de validação de propriedade (Tenant) nos relacionamentos informados (conta/categoria) — deferred, pre-existing
- [x] [Review][Defer] Mutação de histórico financeiro em exclusões (falta de dataFim/soft-delete) — deferred, pre-existing
- [x] [Review][Defer] Falta de paginação nos endpoints `GET` de listagem — deferred, pre-existing
- [x] [Review][Defer] Falta de cobertura de testes automatizados (integração e UI) — deferred, pre-existing

## Verification

**Commands:**
- `cd backend && ./mvnw compile` -- expected: BUILD SUCCESS
- `cd frontend && npx tsc --noEmit` -- expected: sem erros de tipo

**Manual checks (if no CLI):**
- Iniciar backend e frontend. Fazer login. Navegar para `/despesas-fixas`. Criar, editar e excluir uma despesa fixa. Repetir para `/receitas-fixas`. Verificar empty states, validações e toasts.

## Design Notes

As entidades `DespesaFixa` e `ReceitaFixa` são estruturalmente idênticas neste momento. Foram mantidas como entidades separadas (em vez de uma única `MovimentacaoFixa` com enum de tipo) para alinhar com a navegação do UX (abas separadas) e evitar complexidade prematura. Se a Story 3.4 ou o Epic 4 revelarem necessidade de consolidação, o refactor será natural e localizado.

O campo `dataInicio` armazena o primeiro dia do mês de vigência (ex: `2026-08-01`). O frontend usa `<input type="month">` para captura e envia como `YYYY-MM-01`.

## Suggested Review Order

**Backend Core (API & Domínio)**

- Modelagem de domínio com foco em multi-tenancy e relacionamentos persistentes.
  [`DespesaFixa.java:30`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/DespesaFixa.java#L30)

- Rest Controller expondo endpoint CRUD padrão para interface com frontend, interceptando violações de integridade.
  [`DespesaFixaController.java:34`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/DespesaFixaController.java#L34)

**Frontend Data Layer (Hooks & State)**

- Wrapper do React Query lidando com fetch, mutations e CSRF, com validação de payload via `id` na atualização.
  [`useDespesasFixas.ts:58`](../../frontend/src/hooks/useDespesasFixas.ts#L58)

- Wrapper análogo para receitas, fechando o ciclo de mutations.
  [`useReceitasFixas.ts:58`](../../frontend/src/hooks/useReceitasFixas.ts#L58)

**Frontend View Layer (UI & Formulários)**

- Componente de página agregador: lista de lançamentos fixos, estados vazios e integração com modais de deleção/edição.
  [`DespesasFixasPage.tsx:34`](../../frontend/src/pages/DespesasFixas/DespesasFixasPage.tsx#L34)

- Formulário de cadastro/edição com controle controlado por estado, selects de dependência e inicialização de data baseada em Timezone local.
  [`DespesaFixaFormModal.tsx:42`](../../frontend/src/pages/DespesasFixas/components/DespesaFixaFormModal.tsx#L42)

**Integração de Rotas**

- Substituição dos placeholders pelas páginas recém-criadas na árvore de roteamento React Router.
  [`App.tsx:75`](../../frontend/src/App.tsx#L75)
