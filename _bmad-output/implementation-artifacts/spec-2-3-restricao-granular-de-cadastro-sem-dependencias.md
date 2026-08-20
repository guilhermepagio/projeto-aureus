---
title: 'Epic 2 Story 3: Restrição Granular de Cadastro sem Dependências'
type: 'feature'
created: '2026-08-20'
status: 'done'
baseline_commit: '715d61ab5250b8d52e6fb8b884ab213df78e8f10'
review_loop_iteration: 0
context:
  - '{project-root}/_bmad-output/implementation-artifacts/epic-2-context.md'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Os formulários de movimentações financeiras (receitas e despesas) requerem o vínculo com uma Conta e uma Categoria. Se o usuário tentar acessar esses formulários sem ter pelo menos uma conta e uma categoria cadastradas, ele não conseguirá salvar os dados e a experiência será frustrante.

**Approach:** Criar um componente isolado no frontend (`RequiresDependencies`) que atue como um "guard" (wrapper). Ele usará os hooks de contas e categorias para verificar a existência de dados. Se faltarem contas ou categorias, o componente bloqueará a renderização dos formulários e exibirá um "Empty State" com botões de chamada-para-ação (CTA) direcionando o usuário para as telas de cadastro.

## Boundaries & Constraints

**Always:**
- Utilizar os hooks `useContas` e `useCategorias` já existentes para checar o status e os dados.
- Renderizar o estado de loading enquanto as queries estiverem carregando.
- Utilizar ícones semânticos, título claro, descrição de suporte e botões de ação para navegação (React Router `Link`) para `/contas` e `/categorias`.

**Ask First:**
- Se precisar instalar novas dependências (ex: pacotes de ícones), pedir autorização. O projeto já tem padrão para SVGs inline (visto em `CategoriasPage.tsx`).

**Never:**
- Não permitir que o conteúdo "vaze" ou pisque ("flicker") antes de as dependências estarem completamente resolvidas e validadas (renderizar apenas loader, empty state ou os `children`).

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Contas e Categorias carregando | `isLoading` true em qualquer um | Exibe mensagem de carregamento | N/A |
| Nenhuma conta cadastrada | `contas.length === 0` | Exibe Empty State bloqueando form, com CTA para cadastrar conta | N/A |
| Nenhuma categoria cadastrada | `categorias.length === 0` | Exibe Empty State bloqueando form, com CTA para cadastrar categoria | N/A |
| Contas e Categorias cadastradas | Arrays > 0 | Renderiza os `children` | N/A |
| Erro na busca | `isError` true em qualquer hook | Exibe mensagem de erro e convida a recarregar | Exibir block de erro |

</frozen-after-approval>

## Code Map

- `frontend/src/components/RequiresDependencies.tsx` -- Novo componente wrapper que implementa a regra de bloqueio e o empty state.
- `frontend/src/App.tsx` -- Onde as rotas (atualmente placeholders para `/despesas-variaveis`, `/despesas-fixas`, `/receitas-variaveis`, `/receitas-fixas`) serão envelopadas pelo `RequiresDependencies`.

## Tasks & Acceptance

**Execution:**
- [x] `frontend/src/components/RequiresDependencies.tsx` -- Criar componente -- Implementa checagem de loading/erro e verifica `contas.length` e `categorias.length`. Retorna estado vazio com links para configuração caso algo falte.
- [x] `frontend/src/App.tsx` -- Envolver rotas das movimentações -- Importar `RequiresDependencies` e envolver os componentes das rotas `/despesas-variaveis`, `/despesas-fixas`, `/receitas-variaveis`, e `/receitas-fixas`.

**Acceptance Criteria:**
- Given usuário logado e sem contas cadastradas, when acessa a rota `/despesas-variaveis`, then vê a mensagem de bloqueio sugerindo cadastro de conta e não vê o formulário.
- Given usuário logado, sem categorias cadastradas, when acessa `/despesas-fixas`, then vê a mensagem solicitando cadastro de categoria.
- Given usuário com pelo menos uma conta e uma categoria, when acessa as rotas de movimentação, then o conteúdo real da página é exibido sem bloqueios.

## Verification

**Manual checks (if no CLI):**
- Iniciar aplicação frontend e backend.
- Limpar dados de Contas/Categorias no banco ou acessar com um usuário novo.
- Acessar `/despesas-variaveis`. Verificar que o Empty State aparece com botões corretos.
- Criar uma Conta. Acessar `/despesas-variaveis`. Verificar que pede Categoria.
- Criar uma Categoria. Acessar `/despesas-variaveis`. Verificar que o placeholder (`Conteúdo de Despesas Variáveis`) é renderizado com sucesso.

## Suggested Review Order

**Componente Guardião (Wrapper)**

- Componente isolado que bloqueia formulários verificando os hooks de Contas e Categorias.
  [`RequiresDependencies.tsx:13`](../../frontend/src/components/RequiresDependencies.tsx#L13)

- Renderização dos estados vazios (Empty States) bloqueantes com links de navegação e estado de origem.
  [`RequiresDependencies.tsx:29`](../../frontend/src/components/RequiresDependencies.tsx#L29)

**Roteamento (Integração)**

- Envelopamento das rotas placeholder de movimentações financeiras com `RequiresDependencies`.
  [`App.tsx:76`](../../frontend/src/App.tsx#L76)
