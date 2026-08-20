---
title: 'Epic 2 Story 2: Gestão de Categorias (CRUD) com Proteção de Vínculo'
type: 'feature'
created: '2026-08-20'
status: 'done'
baseline_commit: '68fdc90d37928caf5c597ed1b4d19e5e86a16cba'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-2-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O usuário precisa cadastrar e gerenciar as Categorias (origens e destinos classificatórios do dinheiro) para que futuramente possa registrar movimentações financeiras atreladas a elas. A estrutura precisa garantir o isolamento por usuário (multi-tenancy) e preparar a fundação para a proteção de deleção.

**Approach:** Semelhante à Gestão de Contas, criar uma entidade `Categoria` no backend estendendo `TenantAwareEntity`, com endpoints REST para o CRUD. No frontend, criar uma página e componentes de formulário em um Modal, utilizando React Query para integração com o backend e React Hot Toast para feedback visual.

## Boundaries & Constraints

**Always:**
- A entidade `Categoria` deve estender `TenantAwareEntity` para garantir o isolamento multi-tenant pelo Hibernate de forma automática.
- Categorias são "flat" (sem hierarquia de subcategorias).
- No frontend, mutações devem invalidar o cache da listagem no React Query (`['categorias']`).
- O formulário deve ser apresentado dentro de um Modal.

**Ask First:**
- Se for necessário alterar o componente base genérico `Modal` criado na Story 1, peça aprovação antes.

**Never:**
- Não implementar exclusão lógica (soft delete); usar deleção física padrão, pois o ON DELETE RESTRICT do banco futuramente levantará erro de FK constraint (a ser tratado).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Criar categoria | Descrição preenchida | Categoria salva, modal fecha, toast de sucesso, listagem atualizada | Se erro de validação, exibir erro no form |
| Editar categoria | Novos dados na form | Categoria atualizada, modal fecha, toast de sucesso, listagem atualizada | Se erro na API, exibir toast de erro |
| Excluir categoria | Confirmação no modal | Categoria excluída, toast de sucesso, listagem atualizada | Se erro (futuro FK violation), exibir toast informando bloqueio |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java` -- Nova entidade.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java` -- Repositório JPA.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java` -- Novo controller REST.
- `frontend/src/App.tsx` -- Adição da rota de categorias.
- `frontend/src/hooks/useCategorias.ts` -- Novos hooks React Query para buscar, criar, editar e excluir categorias.
- `frontend/src/pages/Categorias/CategoriasPage.tsx` -- Nova página com a listagem (reuso da estrutura visual de Contas).
- `frontend/src/pages/Categorias/components/CategoriaFormModal.tsx` -- Componente do formulário isolado.
- `frontend/src/pages/Categorias/components/DeleteConfirmModal.tsx` -- Componente de confirmação de exclusão.

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java` -- Criar entidade Categoria com id, descricao (String, required), observacoes (String) estendendo TenantAwareEntity.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java` -- Criar repository para Categoria.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java` -- Criar endpoints GET, POST, PUT, DELETE para Categorias (idêntico à estrutura de ContaController).
- [x] `frontend/src/hooks/useCategorias.ts` -- Criar funções fetch e hooks React Query para gerenciar as mutações e buscar a listagem.
- [x] `frontend/src/pages/Categorias/CategoriasPage.tsx` -- Criar a tela principal exibindo empty states e a listagem.
- [x] `frontend/src/pages/Categorias/components/CategoriaFormModal.tsx` -- Criar o modal de criação/edição.
- [x] `frontend/src/pages/Categorias/components/DeleteConfirmModal.tsx` -- Criar o modal de deleção (se não puder reutilizar o genérico).
- [x] `frontend/src/App.tsx` -- Adicionar a rota `/categorias`.

**Acceptance Criteria:**
- Given usuário logado, when acessa a página de categorias, then deve ver a lista de categorias.
- Given form preenchido corretamente, when salva a categoria, then deve exibir toast de sucesso e atualizar a lista.
- Given categoria existente, when clica em excluir e confirma, then a categoria deve sumir da lista e ser excluída do BD.

## Spec Change Log

## Verification

**Manual checks (if no CLI):**
- Iniciar backend e frontend.
- Logar no sistema e navegar para `/categorias`.
- Testar criação, edição e exclusão de Categoria.

## Suggested Review Order

**Backend: Entidade e Repositório**

- Definição da Entidade Categoria com validações e tenant
  [`Categoria.java:20`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Categoria.java#L20)

- Interface do repositório para acesso a dados
  [`CategoriaRepository.java:6`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/repository/CategoriaRepository.java#L6)

**Backend: API Controller**

- Rotas REST e tratamento da exceção de integridade na exclusão
  [`CategoriaController.java:23`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/CategoriaController.java#L23)

**Frontend: Integração**

- Hooks do React Query para cache, listagem e mutações
  [`useCategorias.ts:63`](../../frontend/src/hooks/useCategorias.ts#L63)

**Frontend: UI Componentes**

- Formulário isolado em Modal com controle de tamanho
  [`CategoriaFormModal.tsx:64`](../../frontend/src/pages/Categorias/components/CategoriaFormModal.tsx#L64)

- Modal genérico de confirmação para exclusão
  [`DeleteConfirmModal.tsx:21`](../../frontend/src/pages/Categorias/components/DeleteConfirmModal.tsx#L21)

- Tela principal com listagem de dados e empty state
  [`CategoriasPage.tsx:43`](../../frontend/src/pages/Categorias/CategoriasPage.tsx#L43)

- Roteamento da nova tela protegido por autenticação
  [`App.tsx:39`](../../frontend/src/App.tsx#L39)
