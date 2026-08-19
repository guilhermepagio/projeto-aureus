---
title: 'Epic 2 Story 1: Gestão de Contas (CRUD) com Proteção de Vínculo'
type: 'feature'
created: '2026-08-19'
status: 'done'
baseline_commit: '566adf60908d956ddacdd7f322bc1e74acd5cc18'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-2-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** O usuário precisa cadastrar e gerenciar suas origens de dinheiro (Contas) para que futuramente possa registrar movimentações financeiras atreladas a elas. O sistema precisa garantir o isolamento por usuário (multi-tenancy) e preparar a fundação para a proteção de deleção.

**Approach:** Criar uma entidade `Conta` no backend estendendo `TenantAwareEntity`, com endpoints REST para CRUD. No frontend, criar uma página e componentes de formulário em um Modal, utilizando React Query para integração e React Hot Toast para feedback visual.

## Boundaries & Constraints

**Always:** 
- A entidade `Conta` deve estender `TenantAwareEntity` para garantir o isolamento multi-tenant pelo Hibernate de forma automática.
- Consultas, atualizações e deleções não devem se preocupar com `usuario_id` manualmente, o Hibernate lidará com isso via `@TenantId`.
- No frontend, requisições de mutação (criação, edição, exclusão) devem invalidar o cache da listagem no React Query.
- Formulários devem ser apresentados dentro de um Modal.

**Ask First:** 
- Se a estrutura de componentes visuais genéricos (como o Modal base) ficar muito complexa, peça aprovação do design técnico.

**Never:** 
- Não implementar exclusão lógica (soft delete) manual caso o padrão do projeto não o exija; usar deleção física padrão que lançará erro de violação de FK no futuro.
- Não usar Redux/Zustand para cache de dados do servidor; use exclusivamente o React Query.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Criar conta | Descrição preenchida | Conta salva, modal fecha, toast de sucesso, listagem atualizada | Se erro de validação (ex: descrição vazia), exibir erro no form |
| Editar conta | Novos dados na form | Conta atualizada, modal fecha, toast de sucesso, listagem atualizada | Se erro na API, exibir toast de erro |
| Excluir conta | Confirmação no modal | Conta excluída, toast de sucesso, listagem atualizada | Se erro (futuro FK violation), exibir toast informando bloqueio |

</frozen-after-approval>

## Code Map

- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/TenantAwareEntity.java` -- Base para a entidade Conta herdar o multi-tenancy.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java` -- Nova entidade.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java` -- Repositório JPA.
- `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java` -- Novo controller REST.
- `frontend/src/App.tsx` -- Adição da rota e toaster (já existe, apenas reuso).
- `frontend/src/hooks/useContas.ts` -- Novos hooks React Query para buscar, criar, editar e excluir contas.
- `frontend/src/components/ui/Modal.tsx` -- Novo componente genérico de modal (se não existir, criar um simples com Tailwind).
- `frontend/src/pages/Contas/ContasPage.tsx` -- Nova página com a listagem.
- `frontend/src/pages/Contas/components/ContaFormModal.tsx` -- Componente do formulário isolado no modal.
- `frontend/src/pages/Contas/components/DeleteConfirmModal.tsx` -- Componente de confirmação de exclusão.

## Tasks & Acceptance

**Execution:**
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java` -- Criar entidade Conta com id, descricao (String, required), observacoes (String) estendendo TenantAwareEntity.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java` -- Criar repository.
- [x] `backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java` -- Criar endpoints GET, POST, PUT, DELETE para Contas.
- [x] `frontend/src/components/ui/Modal.tsx` -- Criar componente de modal reusável.
- [x] `frontend/src/hooks/useContas.ts` -- Criar funções fetch e hooks React Query (`useQuery`, `useMutation`).
- [x] `frontend/src/pages/Contas/ContasPage.tsx` -- Criar a tela de listagem de contas com botões de editar/excluir.
- [x] `frontend/src/pages/Contas/components/ContaFormModal.tsx` -- Criar o form de criar/editar em um modal.
- [x] `frontend/src/pages/Contas/components/DeleteConfirmModal.tsx` -- Criar o modal de confirmação.
- [x] `frontend/src/App.tsx` -- Configurar rota `/contas`.

**Acceptance Criteria:**
- Given usuário logado, when acessa a página de contas, then deve ver a lista de contas (vazia inicialmente).
- Given form preenchido, when salva a conta, then deve exibir toast de sucesso e atualizar a lista via React Query.
- Given conta existente, when clica em excluir e confirma, then a conta deve sumir da lista e ser excluída do banco de dados (restrições FK serão testadas em épicos futuros).

## Spec Change Log

## Design Notes

No backend, não precisamos setar manualmente o tenant_id:
```java
// O Hibernate fará isso por nós porque Conta estende TenantAwareEntity
@Entity
public class Conta extends TenantAwareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String descricao;
    private String observacoes;
    // getters e setters
}
```

No frontend, usar React Query para listagem e mutação:
```tsx
const queryClient = useQueryClient();
const { mutate } = useMutation({
  mutationFn: createConta,
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['contas'] });
    toast.success('Conta criada!');
  }
});
```

## Verification

**Manual checks (if no CLI):**
- Iniciar o backend (`./mvnw spring-boot:run` ou pela IDE).
- Iniciar o frontend (`npm run dev`).
- Logar no sistema.
- Navegar para `/contas`.
- Testar criação, edição e exclusão de Conta.
- Verificar no log do Hibernate se a query injeta o `usuario_id` corretamente.

## Suggested Review Order

**Backend: Entidade e Repositório**

- Definição da Entidade Conta com tenant e validações
  [`Conta.java:14`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/domain/Conta.java#L14)

- Interface do repositório
  [`ContaRepository.java:8`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/repository/ContaRepository.java#L8)

**Backend: API Controller**

- Rotas REST com validação e tratamento de integridade
  [`ContaController.java:50`](../../backend/src/main/java/com/guilhermepagio/aureus/backend/controller/ContaController.java#L50)

**Frontend: Integração e Cache**

- Hooks do React Query para mutação e cache
  [`useContas.ts:75`](../../frontend/src/hooks/useContas.ts#L75)

**Frontend: UI Componentes**

- Modal base com trava de scroll corrigida
  [`Modal.tsx:17`](../../frontend/src/components/ui/Modal.tsx#L17)

- Formulário de Criação/Edição com validação maxLength
  [`ContaFormModal.tsx:50`](../../frontend/src/pages/Contas/components/ContaFormModal.tsx#L50)

- Modal de Confirmação de Deleção
  [`DeleteConfirmModal.tsx:65`](../../frontend/src/pages/Contas/components/DeleteConfirmModal.tsx#L65)

- Página principal de listagem com empty state
  [`ContasPage.tsx:30`](../../frontend/src/pages/Contas/ContasPage.tsx#L30)

- Roteamento principal do módulo
  [`App.tsx:32`](../../frontend/src/App.tsx#L32)
